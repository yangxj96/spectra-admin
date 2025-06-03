import { http, HttpResponse } from "msw";
import CommonUtils from "@/utils/CommonUtils.ts";

const prefix = import.meta.env.VITE_API_URL;

export default [
    http.get(prefix + "/api/user/page", () => {
        let records = [] as User[];
        for (let i = 0; i < 15; i++) {
            records.push({
                id: CommonUtils.UUID(),
                username: "sysadmin" + i,
                account: "sysadmin" + i,
                telephone: CommonUtils.getRandom(13_000_000_000, 13_099_999_999).toString(),
                dept: "部门" + i,
                status: i % 2 == 0,
                role: "系统管理员"
            });
        }
        return HttpResponse.json<IResult<Page<User>>>({
            code: 0,
            msg: "success",
            data: {
                current: 1,
                optimize_count_sql: false,
                pages: 15,
                records,
                search_count: false,
                size: 15,
                total: 566
            }
        });
    })
];
