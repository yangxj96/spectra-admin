import { http, HttpResponse } from "msw";
import CommonUtils from "@/utils/CommonUtils.ts";

export default [
    http.get("/api/platform/route/page", () => {
        let records = [] as Route[];
        for (let i = 0; i < 15; i++) {
            records.push({
                id: CommonUtils.UUID(),
                uri: "lb://spectra-serve-" + CommonUtils.getRandom(0, 100),
                order: CommonUtils.getRandom(0, 100),
                predicates: "predicates",
                filters: "filters",
                metadata: "metadata",
                route_id: "spectra-serve-" + CommonUtils.getRandom(0, 100)
            });
        }
        return HttpResponse.json<IResult<Page<Route>>>({
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
