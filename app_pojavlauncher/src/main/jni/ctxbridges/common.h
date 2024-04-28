//
// Created by your_name on 18.10.2023.
//

#ifndef POJAVLAUNCHER_COMMON_H
#define POJAVLAUNCHER_COMMON_H

#include <android/native_window.h>  // Include the necessary header for ANativeWindow

// Use constants instead of #defines for better readability and type-safety
const int STATE_RENDERER_ALIVE = 0;
const int STATE_RENDERER_NEW_WINDOW = 1;

// Use a typedef for the struct only if you're going to use it as a type alias
// Also, use a more descriptive name for the struct
typedef struct {
    int state;  // Use int instead of char for state, as it can hold more values
    ANativeWindow *nativeSurface;
    ANativeWindow *newNativeSurface;
} RenderWindow;

#endif // POJAVLAUNCHER_COMMON_H

