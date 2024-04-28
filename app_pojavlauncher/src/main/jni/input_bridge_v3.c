#include <assert.h>
#include <dlfcn.h>
#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <stdatomic.h>
#include <math.h>

#include "log.h"
#include "utils.h"
#include "environ/environ.h"

#define EVENT_TYPE_CHAR 1000
#define EVENT_TYPE_CHAR_MODS 1001
#define EVENT_TYPE_CURSOR_ENTER 1002
#define EVENT_TYPE_FRAMEBUFFER_SIZE 1004
#define EVENT_TYPE_KEY 1
