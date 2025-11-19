class Solution {
public:
    int lengthOfLongestSubstring(string s) {
        vector<int> index(256, -1);
        int maxLen = 0, left = 0;
        for (int right = 0; right < s.size(); ++right) {
            if (index[s[right]] >= left) {
                left = index[s[right]] + 1;
            }
            maxLen = max(maxLen, right - left + 1);
            index[s[right]] = right;
        }
        return maxLen;
    }
};