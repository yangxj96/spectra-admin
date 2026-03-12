import { onMounted, ref } from "vue";

export function useTable<T>(request: (parameters?: BasePageParams) => Promise<Page<T>>, parameters: BasePageParams) {
    // 分页实体
    const pagination = ref<Pagination>({
        size: 10,
        page: 1,
        page_sizes: [10, 50, 100, 150, 300],
        default_page_size: 10,
        total: 0
    });

    // 表数据
    const table_data = ref<T[]>([]);

    onMounted(() => {
        pagination.value.page = parameters.page_num;
        pagination.value.size = parameters.page_size;
        handleCurrentChange(pagination.value.page);
    });

    /**
     * 处理页码改变
     * @param value 页码
     */
    async function handleCurrentChange(value: number) {
        parameters.page_num = value;
        parameters.page_size = pagination.value.size;
        const result = await request(parameters);
        handleRequestResult(result);
    }

    /**
     * 处理每页数量改变
     * @param value 每页数量
     */
    async function handleSizeChange(value: number) {
        parameters.page_num = pagination.value.page;
        parameters.page_size = value;
        const result = await request(parameters);
        handleRequestResult(result);
    }

    /**
     * 进行一次请求
     */
    async function handlerConditionQuery() {
        parameters.page_num = pagination.value.page;
        parameters.page_size = pagination.value.size;
        const result = await request(parameters);
        handleRequestResult(result);
    }

    /**
     * 处理请求结果
     * @param response 响应内容
     */
    function handleRequestResult(response: Page<T>) {
        table_data.value = response.records ?? [];
        pagination.value.total = response.total ?? 0;
        pagination.value.size = response.size ?? 10;
        pagination.value.page = response.current ?? 1;
    }

    return {
        table_data,
        pagination,
        handleCurrentChange,
        handleSizeChange,
        handlerConditionQuery
    };
}

export default useTable;
