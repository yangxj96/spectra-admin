import { setupWorker } from "msw/browser";
import PlatformApiMock from "@/mocks/handlers/PlatformApiMock.ts";

export default setupWorker(...PlatformApiMock);
