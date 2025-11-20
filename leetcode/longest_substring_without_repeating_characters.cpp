// Coder Agent: Longest Substring Without Repeating Characters (C++)
#include <iostream>
#include <unordered_map>
#include <string>
using namespace std;

class Solution {
public:
    int lengthOfLongestSubstring(string s) {
        unordered_map<char, int> index;
        int maxLen = 0, left = 0;
        for (int right = 0; right < s.size(); ++right) {
            if (index.count(s[right]) && index[s[right]] >= left) {
                left = index[s[right]] + 1;
            }
            index[s[right]] = right;
            maxLen = max(maxLen, right - left + 1);
        }
        return maxLen;
    }
};

// Example usage
int main() {
    Solution sol;
    cout << sol.lengthOfLongestSubstring("abcabcbb") << endl; // Output: 3
    return 0;
}
