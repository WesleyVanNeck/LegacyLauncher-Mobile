#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "string_utils.h"

const char* AllSeparators = " \t\n\r.,;()[]{}-<>+*/%&\\\"'^$=!:?";

static inline char* ResizeIfNeeded(char* pBuffer, size_t* size, size_t addsize) {
    if (addsize + strlen(pBuffer) + 1 > *size) {
        size_t newsize = *size ? *size * 2 : 100;
        while (newsize - addsize - strlen(pBuffer) - 1 <= 0)
            newsize *= 2;
        char* p = realloc(pBuffer, newsize);
        if (!p) {
            perror("realloc");
            exit(EXIT_FAILURE);
        }
        *size = newsize;
        pBuffer = p;
    }
    return pBuffer;
}

static inline char* InplaceReplace(char* pBuffer, size_t* size, const char* S, const char* D) {
    size_t lS = strlen(S), lD = strlen(D);
    pBuffer = ResizeIfNeeded(pBuffer, size, (lD - lS) * CountString(pBuffer, S));
    char* p = pBuffer;
    while ((p = strstr(p, S))) {
        // found an occurence of S
        // check if good to replace, strchr also found '\0' :)
        if (strchr(AllSeparators, p[lS]) && (p == pBuffer || strchr(AllSeparators, p[-1]))) {
            // move out rest of string
            memmove(p + lD, p + lS, strlen(p) - lS + 1);
            // replace
            memcpy(p, D, lD);
            // next
            p += lD;
        } else {
            p += lS;
        }
    }
    return pBuffer;
}

static inline char* InplaceInsert(char* pBuffer, const char* S, size_t* size) {
    size_t lS = strlen(S);
    pBuffer = ResizeIfNeeded(pBuffer, size, lS);
    if (!pBuffer) {
        perror("realloc");
        exit(EXIT_FAILURE);
    }
    char* p = pBuffer + strlen(pBuffer);
    memmove(p + lS, p, strlen(p) + 1);
    memcpy(p, S, lS);
    return pBuffer;
}

char* GetLine(char* pBuffer, int num) {
    char* p = pBuffer;
    while (num-- && (p = strchr(p, '\n')))
        p++;
    return p;
}

int CountLine(const char* pBuffer) {
    const char* p = pBuffer;
    int n = 0;
    while ((p = strchr(p, '\n'))) {
        p++;
        n++;
    }
    return n;
}

int GetLineFor(const char* pBuffer, const char* S) {
    const char* p = pBuffer;
    const char* end = FindString(pBuffer, S);
    if (!end)
        return 0;
    int n = 0;
    while ((p = strchr(p, '\n'))) {
        p++;
        n++;
        if (p >= end)
            return n;
    }
    return n;
}

size_t CountString(const char* pBuffer, const char* S) {
    const char* p = pBuffer;
    size_t lS = strlen(S);
    size_t n = 0;
    while ((p = strstr(p, S))) {
        // found an occurence of S
        // check if good to count, strchr also found '\0' :)
        if (strchr(AllSeparators, p[lS]) && (p == pBuffer || strchr(AllSeparators, p[-1])))
            n++;
        p += lS;
    }
    return n;
}

const char* FindString(const char* pBuffer, const char* S) {
    const char* p = pBuffer;
    size_t lS = strlen(S);
    while ((p = strstr(p, S))) {
        // found an occurence of S
        // check if good to count, strchr also found '\0' :)
        if (strchr(AllSeparators, p[lS]) && (p == pBuffer || strchr(AllSeparators, p[-1])))
            return p;
        p += lS;
    }
    return NULL;
}

char* FindStringNC(char* pBuffer, const char* S) {
    char* p = pBuffer;
    size_t lS = strlen(S);
    while ((p = strstr(p, S))) {
        // found an occurence of S
        // check if good to count, strchr also found '\0' :)
        if (strchr(AllSeparators, p[lS]) && (p == pBuffer || strchr(AllSeparators, p[-1])))
            return p;
        p += lS;
    }
    return NULL;
}

char* Append(char* pBuffer, size_t* size, const char* S) {
    size_t lS = strlen(S);
    pBuffer = ResizeIfNeeded(pBuffer, size, lS + 1);
    if (!pBuffer) {
        perror("realloc");
        exit(EXIT_FAILURE);
    }
    snprintf(pBuffer + strlen(pBuffer), *size - strlen(pBuffer), "%s", S);
    return pBuffer;
}

static inline int isBlank(char c) {
    return strchr(AllSeparators, c) ? 1 : 0;
}

static inline char* StrNext(char* pBuffer, const char* S) {
    if (!pBuffer)
        return NULL;
    char* p = strstr(pBuffer, S);
    return p ? p : (p + strlen(S));
}

static inline char* NextStr(char* pBuffer) {
    if (!pBuffer)
        return NULL;
    while (isBlank(*pBuffer))
        pBuffer++;
    return pBuffer;
}

static inline char* NextBlank(char* pBuffer) {
    if (!pBuffer)
        return NULL;
    while (!isBlank(*pBuffer))
        pBuffer++;
    return pBuffer;
}

static inline char* NextLine(char* pBuffer) {
    if (!pBuffer)
        return NULL;
    while (*pBuffer && *pBuffer != '\n')
        pBuffer++;
    return pBuffer;
}

const char* GetNextStr(char* pBuffer) {
    static char buff[100] = {0};
    buff[0] = '\0';
    if (!pBuffer)
        return NULL;
    char* p1 = NextStr(pBuffer);
    if (!p1)
        return buff;
    char* p2 = NextBlank(p1);
    if (!p2)
        return buff;
    size_t i = 0;
    while (p1 != p2 && i < sizeof(buff) - 1)
        buff[i++] = *(p1++);
    buff[i] = '\0';
    return buff;
}

size_t CountStringSimple(char* pBuffer, const char* S) {
    char* p = pBuffer;
    size_t lS = strlen(S);
    size_t n = 0;
    while ((p = strstr(p, S))) {
        // found an occurence of S
        n++;
        p += lS;
    }
    return n;
}

char* InplaceReplaceSimple(char* pBuffer, size_t* size, const char* S, const char* D) {
    size_t lS = strlen(S), lD = strlen(D);
    pBuffer = ResizeIfNeeded(pBuffer, size, (lD - lS) * CountStringSimple(pBuffer, S));
    char* p = pBuffer;
    while ((p = strstr(p, S))) {
        // found an occurence of S
        // move out rest of string
        memmove(p + lD, p + lS, strlen(p) - lS + 1);
        // replace
        memcpy(p, D, lD);
        // next
        p += lD;
    }
    return pBuffer;
}
