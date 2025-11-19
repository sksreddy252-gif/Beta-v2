class Solution {
public:
    int lengthOfLongestSubstring(string s) {
        vector<int> map(256, 0);
        int left = 0, right = 0, maxLen = 0;
        while (right < s.size()) {
            map[s[right]]++;
            while (map[s[right]] > 1) {
                map[s[left]]--;
                left++;
            }
            maxLen = max(maxLen, right - left + 1);
            right++;
        }
        return maxLen;
    }
};