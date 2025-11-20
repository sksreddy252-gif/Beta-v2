# Python Solution for Longest Substring Without Repeating Characters
def lengthOfLongestSubstring(s: str) -> int:
    seen = {}
    max_len = start = 0
    for end, char in enumerate(s):
        if char in seen and seen[char] >= start:
            start = seen[char] + 1
        seen[char] = end
        max_len = max(max_len, end - start + 1)
    return max_len

# Test cases
if __name__ == "__main__":
    print(lengthOfLongestSubstring("abcabcbb")) # 3
    print(lengthOfLongestSubstring("bbbbb"))    # 1
    print(lengthOfLongestSubstring("pwwkew"))   # 3
    print(lengthOfLongestSubstring(""))         # 0
    print(lengthOfLongestSubstring("au"))       # 2
