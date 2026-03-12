const cache = new Map<string, unknown>();

export function getCache<T>(key: string): T | undefined {
    return cache.get(key) as T;
}

export function setCache(key: string, value: unknown) {
    cache.set(key, value);
}

export function clearCache() {
    cache.clear();
}
