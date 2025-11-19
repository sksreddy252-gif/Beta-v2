# longest_substring.py

def length_of_longest_substring(s: str) -> int:
    char_index = {}
    left = 0
    max_length = 0

    for right, char in enumerate(s):
        if char in char_index and char_index[char] >= left:
            left = char_index[char] + 1
        char_index[char] = right
        max_length = max(max_length, right - left + 1)
    return max_length

# Example usage:
if __name__ == "__main__":
    examples = [
        ("abcabcbb", 3),
        ("bbbbb", 1),
        ("pwwkew", 3),
        ("", 0),
        ("abcdefg", 7),
        ("aab", 2)
    ]
    for s, expected in examples:
        result = length_of_longest_substring(s)
        print(f"Input: '{s}' | Output: {result} | Expected: {expected}")
