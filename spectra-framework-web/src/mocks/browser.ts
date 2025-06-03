import { setupWorker } from "msw/browser";
import UserApiMock from "@/mocks/handlers/UserApiMock.ts";
import SystemApiMock from "@/mocks/handlers/SystemApiMock.ts";
import PlatformApiMock from "@/mocks/handlers/PlatformApiMock.ts";

export default setupWorker(...UserApiMock, ...SystemApiMock, ...PlatformApiMock);
