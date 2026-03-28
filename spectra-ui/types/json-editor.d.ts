export {};

declare global {
    interface JsonObject {
        [key: string]: JsonValue;
    }

    interface JsonArray extends Array<JsonValue> {}

    type JsonValue = string | number | boolean | null | JsonObject | JsonArray;
}
