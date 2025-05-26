import { setupServer } from "msw/node";
import CommonApiMock from "@/mocks/handlers/CommonApiMock.ts";
import UserApiMock from "@/mocks/handlers/UserApiMock.ts";

export default setupServer(...CommonApiMock, ...UserApiMock);
