import { http, HttpResponse } from "msw";
import CommonUtils from "@/utils/CommonUtils.ts";

export default [
    http.post("/api/auth/login", () => {
        return HttpResponse.json<IResult<Token>>({
            code: 0,
            msg: "success",
            data: {
                id: "1909865049464242176",
                username: "sysadmin",
                access_token: CommonUtils.UUID(),
                authorities: ["*"],
                roles: ["sysadmin"]
            }
        });
    }),
    http.post("/api/auth/logout", () => {
        return HttpResponse.json<IResult>({
            code: 0,
            msg: "success"
        });
    })
];
