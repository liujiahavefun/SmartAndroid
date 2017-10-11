//
// Created by liujia on 16/8/17.
//

#ifndef CLOUDWOOD_ANDROID_NEW_CONFIG_H
#define CLOUDWOOD_ANDROID_NEW_CONFIG_H

/*
extern "C"{

#ifdef __cplusplus
 #define __STDC_CONSTANT_MACROS
 #ifdef _STDINT_H
  #undef _STDINT_H
 #endif
 # include <stdint.h>
#endif

}
*/


#ifdef WIN32
#define PROTOCOMM_API	__declspec( dllexport )
#define PROTOLOGIN_API	__declspec( dllexport )
#define PROTOMNET_API	__declspec( dllexport )
#define PROTOVIDEO_API	__declspec( dllexport )
#define PROTOAUDIO_API	__declspec( dllexport )
#define PROTOSTAT_API	__declspec( dllexport )
#define HTTPSDK_API     __declspec( dllexport )
#define UNUSED (void)

#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
//#include <process.h>
#else
#include <unistd.h>
#include <sys/time.h>
#include <netdb.h>
#include <netinet/ip.h>
#include <arpa/inet.h>
#include <linux/tcp.h>
#endif

#include <stdlib.h>
#include <string.h>
#include <time.h>

#ifndef WIN32
#include "util/bytes_swap.h"
#endif

//#include <core/packet.h>
//#include <core/int_types.h>
//#include <core/res.h>

#ifdef IPHONE
#endif

#endif //CLOUDWOOD_ANDROID_NEW_CONFIG_H
