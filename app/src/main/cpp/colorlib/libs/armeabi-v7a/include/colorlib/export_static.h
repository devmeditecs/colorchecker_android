
#ifndef COLORLIB_EXPORT_H
#define COLORLIB_EXPORT_H

#ifdef COLORLIB_STATIC_DEFINE
#  define COLORLIB_EXPORT
#  define COLORLIB_NO_EXPORT
#else
#  ifndef COLORLIB_EXPORT
#    ifdef colorlib_EXPORTS
        /* We are building this library */
#      define COLORLIB_EXPORT 
#    else
        /* We are using this library */
#      define COLORLIB_EXPORT 
#    endif
#  endif

#  ifndef COLORLIB_NO_EXPORT
#    define COLORLIB_NO_EXPORT 
#  endif
#endif

#ifndef COLORLIB_DEPRECATED
#  define COLORLIB_DEPRECATED __attribute__ ((__deprecated__))
#endif

#ifndef COLORLIB_DEPRECATED_EXPORT
#  define COLORLIB_DEPRECATED_EXPORT COLORLIB_EXPORT COLORLIB_DEPRECATED
#endif

#ifndef COLORLIB_DEPRECATED_NO_EXPORT
#  define COLORLIB_DEPRECATED_NO_EXPORT COLORLIB_NO_EXPORT COLORLIB_DEPRECATED
#endif

#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef COLORLIB_NO_DEPRECATED
#    define COLORLIB_NO_DEPRECATED
#  endif
#endif

#endif /* COLORLIB_EXPORT_H */
