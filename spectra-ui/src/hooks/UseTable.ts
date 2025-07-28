import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";

export function useTable<T>(
    request: (parameters?: BasePageParams) => Promise<IResult<Page<T>>>,
    parameters: BasePageParams
) {
    // 分页实体
    const pagination = ref<Pagination>({
        size: 10,
        page: 1,
        page_sizes: [15, 50, 100, 150, 300],
        default_page_size: 15,
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
    function handleCurrentChange(value: number) {
        parameters.page_num = value;
        parameters.page_size = pagination.value.size;
        request(parameters).then(handleRequestResult);
    }

    /**
     * 处理每页数量改变
     * @param value 每页数量
     */
    function handleSizeChange(value: number) {
        parameters.page_num = pagination.value.page;
        parameters.page_size = value;
        request(parameters).then(handleRequestResult);
    }

    /**
     * 进行一次请求
     */
    function handlerConditionQuery() {
        parameters.page_num = pagination.value.page;
        parameters.page_size = pagination.value.size;
        request(parameters).then(handleRequestResult);
    }

    /**
     * 处理请求结果
     * @param response 响应内容
     */
    function handleRequestResult(response: IResult<Page<T>>) {
        if (response.code != 200) {
            ElMessage.success({
                message: response.msg
            });
            return;
        }
        if (response.data) {
            table_data.value = response.data.records as never[];
            pagination.value.total = response.data.total;
            pagination.value.size = response.data.size;
            pagination.value.page = response.data.current;
        }
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
