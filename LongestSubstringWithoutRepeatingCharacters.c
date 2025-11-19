int lengthOfLongestSubstring(char * s){
    int maxLen = 0;
    int start = 0;
    int map[256] = {0}; // ASCII character map
    for(int end = 0; s[end] != '\0'; end++) {
        char ch = s[end];
        while(map[(unsigned char)ch]) {
            map[(unsigned char)s[start]] = 0;
            start++;
        }
        map[(unsigned char)ch] = 1;
        int currLen = end - start + 1;
        if(currLen > maxLen) maxLen = currLen;
    }
    return maxLen;
}