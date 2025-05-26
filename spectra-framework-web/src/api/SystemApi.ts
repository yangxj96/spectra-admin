import http from "@/plugin/request";

export default {
    /**
     * 获取菜单
     */
    async getMenu() {
        const response = await http.get<IResult<Menu[]>>("/api/system/menus");
        return response.data;
    }
};
