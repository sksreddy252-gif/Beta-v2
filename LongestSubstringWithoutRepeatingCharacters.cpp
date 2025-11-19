class Solution {
public:
    int lengthOfLongestSubstring(string s) {
        vector<int> map(256, 0);
        int maxLen = 0, start = 0;
        for (int end = 0; end < s.size(); ++end) {
            while (map[s[end]]) {
                map[s[start]] = 0;
                ++start;
            }
            map[s[end]] = 1;
            maxLen = max(maxLen, end - start + 1);
        }
        return maxLen;
    }
};