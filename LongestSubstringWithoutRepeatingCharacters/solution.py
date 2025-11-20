# Coder Agent - Python Solution for Longest Substring Without Repeating Characters
# Leetcode Style

def lengthOfLongestSubstring(s: str) -> int:
    char_index = {}
    left = 0
    max_len = 0
    for right, char in enumerate(s):
        if char in char_index and char_index[char] >= left:
            left = char_index[char] + 1
        char_index[char] = right
        max_len = max(max_len, right - left + 1)
    return max_len

# Test cases
if __name__ == "__main__":
    test_cases = [
        ("abcabcbb", 3),
        ("bbbbb", 1),
        ("pwwkew", 3),
        ("", 0),
        ("abcdefg", 7),
        ("aab", 2),
        ("dvdf", 3)
    ]
    for s, expected in test_cases:
        result = lengthOfLongestSubstring(s)
        print(f"Input: '{s}' | Output: {result} | Expected: {expected} | Pass: {result == expected}")
