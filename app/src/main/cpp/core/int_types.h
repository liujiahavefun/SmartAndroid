//
// Created by liujia on 16/8/21.
//

#ifndef CLOUDWOOD_ANDROID_NEW_INT_TYPES_H
#define CLOUDWOOD_ANDROID_NEW_INT_TYPES_H

#include <sys/types.h>

/* Even in pure C, we still need a standard boolean typedef */
#ifndef     __cplusplus
typedef unsigned char bool;
#define     true    1
#define     false   0
#endif	    /* !__cplusplus */

#ifdef	    __GNUC__
/* FreeBSD has these C99 int types defined in /sys/inttypes.h already */
/*
#ifndef     _SYS_INTTYPES_H_
typedef signed char int8_t;
typedef signed short int16_t;
//typedef signed int int32_t;
typedef signed long long int64_t;
typedef unsigned char uint8_t;
typedef unsigned short uint16_t;
typedef unsigned int uint32_t;
typedef unsigned long long uint64_t;
#endif
*/
#include <stdint.h>
#elif	    defined(_MSC_VER)
typedef signed char int8_t;
typedef signed short int16_t;
typedef signed int int32_t;
typedef signed __int64 int64_t;
typedef unsigned char uint8_t;
typedef unsigned short uint16_t;
typedef unsigned int uint32_t;
typedef unsigned __int64 uint64_t;
#endif /* _MSC_VER */


#define URI_TYPE int32_t


#endif //CLOUDWOOD_ANDROID_NEW_INT_TYPES_H
