// Coder Agent - Java Solution for Longest Substring Without Repeating Characters
// Leetcode Style
public class Solution {
    public int lengthOfLongestSubstring(String s) {
        int[] index = new int[128]; // ASCII
        int maxLen = 0, left = 0;
        for (int right = 0; right < s.length(); right++) {
            left = Math.max(index[s.charAt(right)], left);
            maxLen = Math.max(maxLen, right - left + 1);
            index[s.charAt(right)] = right + 1;
        }
        return maxLen;
    }
    public static void main(String[] args) {
        Solution sol = new Solution();
        String[] testCases = {"abcabcbb", "bbbbb", "pwwkew", "", "abcdefg", "aab", "dvdf"};
        int[] expected = {3, 1, 3, 0, 7, 2, 3};
        for (int i = 0; i < testCases.length; i++) {
            int result = sol.lengthOfLongestSubstring(testCases[i]);
            System.out.println("Input: '" + testCases[i] + "' | Output: " + result + " | Expected: " + expected[i] + " | Pass: " + (result == expected[i]));
        }
    }
}
