// C Solution for Longest Substring Without Repeating Characters
#include <stdio.h>
#include <string.h>

int lengthOfLongestSubstring(char *s) {
    int index[256];
    for (int i = 0; i < 256; i++) index[i] = -1;
    int max_len = 0, start = 0;
    for (int end = 0; s[end]; end++) {
        unsigned char c = s[end];
        if (index[c] >= start) {
            start = index[c] + 1;
        }
        index[c] = end;
        if (end - start + 1 > max_len) max_len = end - start + 1;
    }
    return max_len;
}

// Test cases
int main() {
    printf("%d\n", lengthOfLongestSubstring("abcabcbb")); // 3
    printf("%d\n", lengthOfLongestSubstring("bbbbb"));    // 1
    printf("%d\n", lengthOfLongestSubstring("pwwkew"));   // 3
    printf("%d\n", lengthOfLongestSubstring("") );        // 0
    printf("%d\n", lengthOfLongestSubstring("au"));       // 2
    return 0;
}
