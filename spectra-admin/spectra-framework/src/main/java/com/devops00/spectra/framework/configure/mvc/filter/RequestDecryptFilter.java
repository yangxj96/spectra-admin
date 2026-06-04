//package com.devops00.spectra.framework.configure.mvc.filter;
//
//
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletRequestWrapper;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import java.io.BufferedReader;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//
//
/////
///// 请求解密过滤器
/////
///// @author Jack Young
///// @version 1.0
///// @since 2026/6/4 17:33
/////
//@Order(1)
//@Component
//public class RequestDecryptFilter implements Filter {
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//
//        // 判断是否需要解密（比如排除登录接口、获取公钥接口）
//        if (isExcludedUrl(httpRequest.getRequestURI())) {
//            chain.doFilter(request, response);
//            return;
//        }
//
//        // 包装 Request，重写 getInputStream() 和 getReader()
//        RequestDecryptWrapper wrappedRequest = new RequestDecryptWrapper(httpRequest, aesKeyService);
//
//        // 继续传递包装后的 Request
//        chain.doFilter(wrappedRequest, response);
//    }
//
//    /**
//     * 实现不需要解密的部分
//     *
//     * @param url 请求的url
//     * @return 是否不需要解密
//     */
//    private boolean isExcludedUrl(String url) {
//        return false;
//    }
//
//    /**
//     * 请求解密包装器
//     */
//    public class RequestDecryptWrapper extends HttpServletRequestWrapper {
//
//        private final byte[] decryptedBody;
//
//        public RequestDecryptWrapper(HttpServletRequest request, AesKeyService aesKeyService) {
//            super(request);
//            this.decryptedBody = decryptRequestBody(request, aesKeyService);
//        }
//
//        @Override
//        public ServletInputStream getInputStream() {
//            ByteArrayInputStream bais = new ByteArrayInputStream(decryptedBody);
//            return new ServletInputStream() {
//
//                @Override
//                public boolean isFinished() {
//                    return bais.available() == 0;
//                }
//
//                @Override
//                public boolean isReady() {
//                    return true;
//                }
//
//                @Override
//                public void setReadListener(ReadListener listener) {
//
//                }
//
//                @Override
//                public int read() {
//                    return bais.read();
//                }
//            };
//        }
//
//        @Override
//        public BufferedReader getReader() {
//            return new BufferedReader(new InputStreamReader(getInputStream()));
//        }
//    }
//}
