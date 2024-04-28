/*
 * Mesa 3-D graphics library - Off-Screen rendering interface.
 *
 * This is an operating system and window system independent interface to
 * Mesa which allows one to render images into a client-supplied buffer in
 * main memory.  Such images may be manipulated or saved in whatever way the
 * client wants.
 *
 * For more information, see the documentation at <https://docs.mesa3d.org/>
 */

#ifndef OSMESA_H
#define OSMESA_H

#ifdef __cplusplus
extern "C" {
#endif

#include <GL/gl.h>

/**
 * Major and minor version numbers of the OSMESA library.
 */
#define OSMESA_MAJOR_VERSION 11
#define OSMESA_MINOR_VERSION 2
#define OSMESA_PATCH_VERSION 0

/**
 * Values for the format parameter of OSMesaCreateContext().
 * New in version 2.0.
 */
typedef enum {
    OSMESA_COLOR_INDEX = GL_COLOR_INDEX,
    OSMESA_RGBA = GL_RGBA,
    OSMESA_BGRA = 0x1,
    OSMESA_ARGB = 0x2,
    OSMESA_RGB = GL_RGB,
    OSMESA_BGR = 0x4,
    OSMESA_RGB_565 = 0x5
} OSMesaFormat;

/**
 * OSMesaPixelStore() parameters:
 * New in version 2.0.
 */
typedef enum {
    OSMESA_ROW_LENGTH = 0x10,
    OSMESA_Y_UP = 0x11
} OSMesaPixelStoreParam;

/**
 * Accepted by OSMesaGetIntegerv:
 */
typedef enum {
    OSMESA_WIDTH = 0x20,
    OSMESA_HEIGHT = 0x21,
    OSMESA_FORMAT = 0x22,
    OSMESA_TYPE = 0x23,
    OSMESA_MAX_WIDTH = 0x24,  /* new in 4.0 */
    OSMESA_MAX_HEIGHT = 0x25  /* new in 4.0 */
} OSMesaGetIntegervParam;

/**
 * Accepted in OSMesaCreateContextAttrib's attribute list.
 */
typedef enum {
    OSMESA_DEPTH_BITS = 0x30,
    OSMESA_STENCIL_BITS = 0x31,
    OSMESA_ACCUM_BITS = 0x32,
    OSMESA_PROFILE = 0x33,
    OSMESA_CORE_PROFILE = 0x34,
    OSMESA_COMPAT_PROFILE = 0x35,
    OSMESA_CONTEXT_MAJOR_VERSION = 0x36,
    OSMESA_CONTEXT_MINOR_VERSION = 0x37
} OSMesaCreateContextAttrib;

/**
 * Type definition for an Off-Screen Mesa rendering context.
 */
typedef struct osmesa_context *OSMesaContext;

/**
 * Create an Off-Screen Mesa rendering context.
 *
 * @param format The pixel format of the context.
 * @param sharelist Specifies another OSMesaContext with which to share
 *                  display lists. NULL indicates no sharing.
 *
 * @return An OSMesaContext or 0 if error.
 */
GLAPI OSMesaContext GLAPIENTRY
OSMesaCreateContext(GLenum format, OSMesaContext sharelist);

/**
 * Create an Off-Screen Mesa rendering context and specify desired
 * size of depth buffer, stencil buffer and accumulation buffer.
 *
 * @param format The pixel format of the context.
 * @param depthBits The desired size of the depth buffer, in bits.
 * @param stencilBits The desired size of the stencil buffer, in bits.
 * @param accumBits The desired size of the accumulation buffer, in bits.
 * @param sharelist Specifies another OSMesaContext with which to share
 *                  display lists. NULL indicates no sharing.
 *
 * @return An OSMesaContext or 0 if error.
 */
GLAPI OSMesaContext GLAPIENTRY
OSMesaCreateContextExt(GLenum format, GLint depthBits, GLint stencilBits,
                       GLint accumBits, OSMesaContext sharelist);

/**
 * Create an Off-Screen Mesa rendering context with attribute list.
 *
 * @param attribList A list of attribute-value pairs, terminated with
 *                   attribute==0. Supported Attributes:
 *                   - OSMESA_FORMAT: The pixel format of the context.
 *                   - OSMESA_DEPTH_BITS: The desired size of the depth buffer,
 *                     in bits.
 *                   - OSMESA_STENCIL_BITS: The desired size of the stencil buffer,
 *                     in bits.
 *                   - OSMESA_ACCUM_BITS: The desired size of the accumulation buffer,
 *                     in bits.
 *                   - OSMESA_PROFILE: The desired OpenGL profile.
 *                   - OSMESA_CONTEXT_MAJOR_VERSION: The desired major version of
 *                     the OpenGL context.
 *                   - OSMESA_CONTEXT_MINOR_VERSION: The desired minor version of
 *                     the OpenGL context.
 *
 * @param sharelist Specifies another OSMesaContext with which to share
 *                  display lists. NULL indicates no sharing.
 *
 * @return An OSMesaContext or 0 if error.
 */
GLAPI OSMesaContext GLAPIENTRY
OSMesaCreateContextAttribs(const int *attribList, OSMesaContext sharelist);

/**
 * Destroy an Off-Screen Mesa rendering context.
 *
 * @param ctx The rendering context to destroy.
 */
GLAPI void GLAPIENTRY
OSMesaDestroyContext(OSMesaContext ctx);

/**
 * Bind an OSMesaContext to an image buffer.
 *
 * @param ctx The rendering context.
 * @param buffer The image buffer memory.
 * @param type The data type of the pixel components.
 * @param width The width of the image buffer, in pixels.
 * @param height The height of the image buffer, in pixels.
 *
 * @return GL_TRUE if success, GL_FALSE if error.
 */
GLAPI GLboolean GLAPIENTRY
OSMesaMakeCurrent(OSMesaContext ctx, void *buffer, GLenum type,
                  GLsizei width, GLsizei height);

/**
 * Return the current Off-Screen Mesa rendering context handle.
 */
GLAPI OSMesaContext GLAPIENTRY
OSMesaGetCurrentContext(void);

/**
 * Set pixel store/packing parameters for the current context.
 *
 * @param pname The pixel store parameter.
 * @param value The value for the parameter pname.
 */
GLAPI void GLAPIENTRY
OSMesaPixelStore(GLint pname, GLint value);

/**
 * Return an integer value like glGetIntegerv.
 *
 * @param pname The parameter to query.
 * @param value The integer in which to return the result.
 */
GLAPI void GLAPIENTRY
OSMesaGetIntegerv(GLint pname, GLint *value);

/**
 * Return the depth buffer associated with an OSMesa context.
 *
 * @param c The OSMesa context.
 * @param width The width of the depth buffer, in pixels.
 * @param height The height of the depth buffer, in pixels.
 * @param bytesPerValue The number of bytes per depth value.
 * @param buffer A pointer to the depth buffer values.
 *
 * @return GL_TRUE or GL_FALSE to indicate success or failure.
 */
GLAPI GLboolean GLAPIENTRY
OSMesaGetDepthBuffer(OSMesaContext c, GLint *width, GLint *height,
                     GLint *bytesPerValue, void **buffer);

/**
 * Return the color buffer associated with an OSMesa context.
 *
 * @param c The OSMesa context.
 * @param width The width of the color buffer, in pixels.
 * @param height The height of the color buffer, in pixels.
 * @param format A pointer to the format of the color buffer.
 * @param buffer A pointer to the color buffer values.
 *
 * @return GL_TRUE or GL_FALSE to indicate success or failure.
 */
GLAPI GLboolean GLAPIENTRY
OSMesaGetColorBuffer(OSMesaContext c, GLint *width, GLint *height,
                     GLint *format, void **buffer);

/**
 * Typedef for the named function pointer.
 */
typedef void (*OSMESAproc)();

/**
 * Return pointer to the named function.
 *
 * @param funcName The name of the function to retrieve.
 *
 * @return The function pointer or NULL if not found.
 */
GLAPI OSMESAproc GLAPIENTRY
OSMesaGetProcAddress(const char *funcName);

/**
 * Enable/disable color clamping, off by default.
 *
 * @param enable Enable or disable color clamping.
 */
GLAPI void GLAPIENTRY
OSMesaColorClamp(GLboolean enable);

/**
 * Enable/disable Gallium post-process filters.
 *
 * @param osmesa The OSMesa context.
 * @param filter The name of the filter to enable or disable.
 * @param enable_value Enable or disable the filter.
 */
GLAPI void GLAPIENTRY
OSMesaPostprocess(OSMesaContext osmesa, const char *filter,
                  unsigned enable_value);

#ifdef __cplusplus
}
#endif

#endif /* OSMESA_H */
