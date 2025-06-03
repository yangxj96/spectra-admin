import { setupWorker } from "msw/browser";
import UserApiMock from "@/mocks/handlers/UserApiMock.ts";
import PlatformApiMock from "@/mocks/handlers/PlatformApiMock.ts";

export default setupWorker(...UserApiMock, ...PlatformApiMock);
