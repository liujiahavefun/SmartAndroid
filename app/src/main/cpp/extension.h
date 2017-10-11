//
// Created by liujia on 16/8/17.
//

#ifndef CLOUDWOOD_ANDROID_NEW_EXTENSION_H
#define CLOUDWOOD_ANDROID_NEW_EXTENSION_H

#ifdef _WIN32
#include "config.h"

#ifndef uint8_t
typedef unsigned char uint8_t;
#endif

#ifndef uint16_t
typedef unsigned short uint16_t;
#endif

#ifndef uint32_t
typedef unsigned int uint32_t;
#endif

#ifndef uint64_t
typedef unsigned __int64 uint64_t;
#endif

#else
#include <stdint.h>
#include <arpa/inet.h>
#endif

namespace NetEngine
{
    struct Extension {
        int extID;	// the identify of the extension
    };

    struct ExtCompress : Extension {
        static const int LZO	= 0;
        static const int GZIP	= 1;

        static const int EXTID	= 0;

        int CommType;
        int CommLevel;
        ExtCompress(){
            extID		= EXTID;
            CommLevel	= 0;
            CommType	= LZO;
        }
    };

    struct ExtProxy : Extension {
        static const int SOCK5	= 0;
        static const int HTTP	= 1;

        static const int EXTID	= 1;

        int		Type;
        int		Ip;
        short	Port;
        char	username[256];
        char	passwd[256];
        ExtProxy(){
            extID	= EXTID;
            Type	= SOCK5;
            Ip		= INADDR_ANY;
            Port	= 0;
            username[0] = 0;
            passwd[0]	= 0;
        }
    };

    struct ExtEncryption : Extension {
        static const int RC4	= 0;
        static const int AES	= 1;

        static const int EXTID	= 2;

        int		CipherType;
        // the URI for key exchange
        uint32_t	URI;
        uint32_t	ResURI;
        // may have more information need to provide.

        ExtEncryption()
                : URI(0)
                , ResURI(0)
        {
            extID		= EXTID;
            CipherType	= RC4;
        }
    };

    struct ExtMultiPort : Extension {
        static const int EXTID	= 3;

        short Ports[4];
        ExtMultiPort(){
            extID = EXTID;
            Ports[0] = 0;
        }
    };

    struct ExtUDT : public Extension
    {
        static const int EXTID = 4;
        static const int CONN_CLIENT = 0;
        static const int CONN_SERVER = 1;

        int m_nUdtRole;//CONN_CLIENT or CONN_SERVER
        int m_nMethod;//E_P2P_METHOD
        uint32_t m_uService;
        uint32_t m_uMyUid;
        uint32_t m_uPeerUid;
        uint16_t m_wChanNum;

        ExtUDT()
        {
            extID = EXTID;
            m_nUdtRole = CONN_SERVER;
            m_nMethod = 1;
            m_uService = 0;
            m_uMyUid = 0;
            m_uPeerUid = 0;
            m_wChanNum = 0xffff;
        }
    };

    struct ExtDirect : Extension
    {
        static const int EXTID = 5;

        ExtDirect()
        {
            extID = EXTID;
        }
    };

    extern struct ExtEncryption EXT_ENCRYPT_INITIALIZER;
    extern struct ExtProxy		EXT_PROXY_INITIALIZER;
    extern struct ExtProxy		EXT_COMMPRESS_INITIALIZER;
    extern struct ExtMultiPort	EXT_MULTIPORT_INITIALIZER;
    extern struct ExtDirect     EXT_DIRECT_INITIALIZER;
}

#endif //CLOUDWOOD_ANDROID_NEW_EXTENSION_H
