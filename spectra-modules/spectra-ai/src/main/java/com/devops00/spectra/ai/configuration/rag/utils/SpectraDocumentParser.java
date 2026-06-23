package com.devops00.spectra.ai.configuration.rag.utils;

import com.devops00.spectra.common.constant.LogPrefix;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.parser.markdown.MarkdownDocumentParser;
import dev.langchain4j.data.document.parser.yaml.YamlDocumentParser;
import dev.langchain4j.data.document.transformer.jsoup.HtmlToTextDocumentTransformer; // 或者 JsoupDocumentTransformer
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/// 文档解析器
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/6/15 17:29
@Slf4j
public class SpectraDocumentParser implements DocumentParser {

    // 扩充预实例化解析器，保持高并发下的 GC 友好
    // @formatter:off
    private static final DocumentParser PDF_PARSER  = new ApachePdfBoxDocumentParser();
    private static final DocumentParser POI_PARSER  = new ApachePoiDocumentParser();
    private static final DocumentParser TIKA_PARSER = new ApacheTikaDocumentParser();
    private static final DocumentParser TEXT_PARSER = new TextDocumentParser();
    private static final DocumentParser MD_PARSER   = new MarkdownDocumentParser();
    private static final DocumentParser YAML_PARSER = new YamlDocumentParser();
    // @formatter:on

    // 建立更精准的后缀与解析器的静态映射路由表
    private static final Map<String, DocumentParser> ROUTER_MAP = new HashMap<>();

    // 定义拦截黑名单
    private static final Set<String> BLACKLIST_IMAGE = Set.of("png", "jpg", "jpeg", "bmp", "gif", "tiff");
    private static final Set<String> BLACKLIST_MEDIA = Set.of("mp3", "mp4", "avi", "wav", "zip", "rar", "7z", "tar", "gz");

    static {
        // PDF 专有高精度
        ROUTER_MAP.put("pdf", PDF_PARSER);

        // Office 系列
        for (String ext : Set.of("docx", "doc", "xlsx", "xls", "pptx", "ppt")) {
            ROUTER_MAP.put(ext, POI_PARSER);
        }

        // HTML依然用TIKA提取，但在后面我们会配合Jsoup进一步清洗
        ROUTER_MAP.put("html", TIKA_PARSER);
        ROUTER_MAP.put("htm", TIKA_PARSER);

        // Markdown升级为专属语义解析器
        ROUTER_MAP.put("md", MD_PARSER);

        // YAML升级为专属结构化解析器
        ROUTER_MAP.put("yml", YAML_PARSER);
        ROUTER_MAP.put("yaml", YAML_PARSER);

        // 其余代码文件与纯文本，保持轻量级的 Text 解析
        for (String ext : Set.of("txt", "java", "json", "xml", "csv", "sh", "py", "go", "sql")) {
            ROUTER_MAP.put(ext, TEXT_PARSER);
        }
    }

    @Override
    public Document parse(InputStream inputStream) {
        throw new UnsupportedOperationException("请调用带有文件名或后缀的方法parse(inputStream, filename)");
    }

    /**
     * 根据文件名后缀，智能路由到对应的专有高精度解析器，并注入增强元数据
     */
    public Document parse(InputStream inputStream, String filename) {
        // 默认兜底后缀
        String suffix = "txt";

        if (filename != null && filename.contains(".")) {
            suffix = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase().trim();
        }

        // 黑名单前置拦截
        if (BLACKLIST_IMAGE.contains(suffix)) {
            log.error("{} 文件 [{}] 解析被拦截: 暂不支持图片类型的 RAG 索引构建", LogPrefix.AI.p(), filename);
            throw new IllegalArgumentException("当前系统暂不支持图片类型的RAG索引构建，请上传文本格式文档");
        }
        if (BLACKLIST_MEDIA.contains(suffix)) {
            log.error("{} 文件 [{}] 解析被拦截: 暂不支持多媒体或压缩包格式", LogPrefix.AI.p(), filename);
            throw new IllegalArgumentException("暂不支持多媒体或压缩包格式解析");
        }

        // 智能路由到具体解析器
        DocumentParser delegate = ROUTER_MAP.getOrDefault(suffix, TIKA_PARSER);
        log.debug("{} 文件 [{}] 命中解析器: {}", LogPrefix.AI.p(), filename, delegate.getClass().getSimpleName());

        Document document;
        try {
            document = delegate.parse(inputStream);

            // 针对HTML/HTM文件，利用最新规范的Jsoup转换器进行二次深度清洗
            if (document != null && ("html".equals(suffix) || "htm".equals(suffix))) {
                log.info("{} 正在使用最新 HtmlToTextDocumentTransformer 深度清洗 HTML 网页噪声...", LogPrefix.AI.p());
                HtmlToTextDocumentTransformer transformer = new HtmlToTextDocumentTransformer();
                document = transformer.transform(document);
            }

        } catch (Exception e) {
            log.error("{} 底层解析器 [{}] 解析文件 [{}] 发生核心崩溃", LogPrefix.AI.p(), delegate.getClass().getSimpleName(), filename, e);
            throw new RuntimeException("文档物理结构损坏，解析失败", e);
        }

        // 空文本防御机制：防止LangChain4j抛出BlankDocumentException导致流程中断
        if (document == null || document.text() == null || document.text().isBlank()) {
            log.warn("{} 警告：文件 [{}] 解析出的文本内容为空（可能为全图扫描件或空白文件）", LogPrefix.AI.p(), filename);
            Metadata emptyMetadata = createBaseMetadata(filename, suffix);
            return Document.from("[无法从该文档中提取任何有效文本，可能由于该文档是纯图片扫描件、无文本层的 PDF 或本身就是空文件。]", emptyMetadata);
        }

        // 元数据清洗与增强注入
        enrichMetadata(document, filename, suffix);
        return document;
    }

    /**
     * 构建基础元数据
     */
    private Metadata createBaseMetadata(String filename, String suffix) {
        Metadata metadata = new Metadata();
        metadata.put("file_name", filename != null ? filename : "unknown");
        metadata.put("file_type", suffix);
        metadata.put("imported_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return metadata;
    }

    /**
     * 为解析成功的 Document 注入和补充业务元数据
     */
    private void enrichMetadata(Document document, String filename, String suffix) {
        Metadata metadata = document.metadata();
        if (!metadata.containsKey("file_name") && filename != null) {
            metadata.put("file_name", filename);
        }
        metadata.put("file_type", suffix);
        metadata.put("imported_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}