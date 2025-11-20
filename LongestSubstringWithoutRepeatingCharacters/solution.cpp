// Coder Agent - C++ Solution for Longest Substring Without Repeating Characters
// Leetcode Style
#include <iostream>
#include <unordered_map>
#include <string>
using namespace std;

class Solution {
public:
    int lengthOfLongestSubstring(string s) {
        unordered_map<char, int> charIndex;
        int left = 0, maxLen = 0;
        for (int right = 0; right < s.size(); ++right) {
            if (charIndex.count(s[right]) && charIndex[s[right]] >= left) {
                left = charIndex[s[right]] + 1;
            }
            charIndex[s[right]] = right;
            maxLen = max(maxLen, right - left + 1);
        }
        return maxLen;
    }
};

// Test cases
int main() {
    Solution sol;
    pair<string, int> testCases[] = {
        {"abcabcbb", 3},
        {"bbbbb", 1},
        {"pwwkew", 3},
        {"", 0},
        {"abcdefg", 7},
        {"aab", 2},
        {"dvdf", 3}
    };
    for (auto& tc : testCases) {
        int result = sol.lengthOfLongestSubstring(tc.first);
        cout << "Input: '" << tc.first << "' | Output: " << result << " | Expected: " << tc.second << " | Pass: " << (result == tc.second) << endl;
    }
    return 0;
}
