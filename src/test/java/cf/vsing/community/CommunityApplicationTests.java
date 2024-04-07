package cf.vsing.community;

import org.junit.Test;

import java.util.ArrayList;


class CommunityApplicationTests {
    @Test
    private int strToInt(String str) {
        int result;
        str = str.trim();
        boolean sign = false;
        int s = 1;
        int ans = 0;
        ArrayList<Integer> book = new ArrayList<Integer>();
        for (Character i : str.toCharArray()) {
            if ((i == '+' || i == '-') && !sign) {
                s = i == '-' ? -1 : 1;
                sign = true;
            } else if (i > '0' && i < '9') {
                book.add(i - '0');
            } else
                break;
        }
        if (book.size() == 0) {
            result = 0;
        } else if (book.size() > 10) {
            result = s == '-' ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        } else {
            for (int i = 0; i < book.size(); i++) {
                ans += i * Math.pow(10, book.size() - i - 1);
            }
            ans = ans * s;
            result = ans > 0 ? Math.min(ans, Integer.MAX_VALUE) : Math.max(ans, Integer.MIN_VALUE);
        }

        return result;
    }
}
