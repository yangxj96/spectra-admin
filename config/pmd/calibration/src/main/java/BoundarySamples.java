final class BoundarySamples {
    int cyclo15(int value) {
        int result = 0;
        if (value > 0) result++;
        if (value > 1) result++;
        if (value > 2) result++;
        if (value > 3) result++;
        if (value > 4) result++;
        if (value > 5) result++;
        if (value > 6) result++;
        if (value > 7) result++;
        if (value > 8) result++;
        if (value > 9) result++;
        if (value > 10) result++;
        if (value > 11) result++;
        if (value > 12) result++;
        if (value > 13) result++;
        return result;
    }

    int cyclo16(int value) {
        int result = 0;
        if (value > 0) result++;
        if (value > 1) result++;
        if (value > 2) result++;
        if (value > 3) result++;
        if (value > 4) result++;
        if (value > 5) result++;
        if (value > 6) result++;
        if (value > 7) result++;
        if (value > 8) result++;
        if (value > 9) result++;
        if (value > 10) result++;
        if (value > 11) result++;
        if (value > 12) result++;
        if (value > 13) result++;
        if (value > 14) result++;
        return result;
    }

    void params5(int a, int b, int c, int d, int e) {}

    void params6(int a, int b, int c, int d, int e, int f) {}

    int nested3(int value) {
        if (value > 0) {
            while (value < 10) {
                switch (value) {
                    case 1 -> value++;
                    default -> value += 2;
                }
            }
        }
        return value;
    }

    int nested4(int value) {
        if (value > 0) {
            while (value < 10) {
                switch (value) {
                    case 1 -> {
                        if (value < 5) value++;
                    }
                    default -> value += 2;
                }
            }
        }
        return value;
    }

    int ifNested3(int value) {
        if (value > 0) {
            if (value > 1) {
                if (value > 2) value++;
            }
        }
        return value;
    }

    int ifNested4(int value) {
        if (value > 0) {
            if (value > 1) {
                if (value > 2) {
                    if (value > 3) value++;
                }
            }
        }
        return value;
    }

    int npath200(int value) {
        int result = 0;
        switch (value) {
            case 1 -> result += 1;
            case 2 -> result += 2;
            case 3 -> result += 3;
            case 4 -> result += 4;
            default -> result += 5;
        }
        if (value > 0) result++;
        if (value > 1) result++;
        if (value > 2) result++;
        switch (value) {
            case 5 -> result += 1;
            case 6 -> result += 2;
            case 7 -> result += 3;
            case 8 -> result += 4;
            default -> result += 5;
        }
        return result;
    }

    int npath201(int value) {
        if (value > 0) {
            int result = 0;
            switch (value) {
                case 1 -> result += 1;
                case 2 -> result += 2;
                case 3 -> result += 3;
                case 4 -> result += 4;
                default -> result += 5;
            }
            if (value > 0) result++;
            if (value > 1) result++;
            if (value > 2) result++;
            switch (value) {
                case 5 -> result += 1;
                case 6 -> result += 2;
                case 7 -> result += 3;
                case 8 -> result += 4;
                default -> result += 5;
            }
            return result;
        }
        return value;
    }
}
