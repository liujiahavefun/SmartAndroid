//
// Created by liujia on 16/8/20.
//

#ifndef CLOUDWOOD_ANDROID_NEW_OPENSSL_PROXY_H
#define CLOUDWOOD_ANDROID_NEW_OPENSSL_PROXY_H

#include <openssl/md5.h>
#include <openssl/aes.h>
#include <openssl/evp.h>
#include <openssl/rsa.h>
#include <openssl/pem.h>

#include <openssl/rand.h>
#include <openssl/crypto.h>
#include <openssl/err.h>
#include <openssl/rand.h>
#include <openssl/bn.h>
#include <openssl/blowfish.h>
#include <openssl/hmac.h>
#include <openssl/x509.h>
#include <openssl/x509v3.h>
//#include <openssl/ssl.h>

#include <openssl/rc4.h>

namespace Netmod
{
	class BaseFunc
	{
	public:
		//datareport
		//dwEVP_Digest
		static int	netmodedgt(const void *data, size_t count,
			unsigned char *md, unsigned int *size, const EVP_MD *type, ENGINE *imp);
	#ifndef OPENSSL_NO_MD5
		//dwEVP_md5
		static const EVP_MD *netmodem(void);
	#endif
		//dwAES_set_encrypt_key
		static int netmodasek(const unsigned char *userKey, const int bits,
			AES_KEY *key);
		//dwAES_set_decrypt_key
		static int netmodasdk(const unsigned char *userKey, const int bits,
			AES_KEY *key);
		//dwAES_cbc_encrypt
		static void netmodace(const unsigned char *in, unsigned char *out,
			size_t length, const AES_KEY *key,
			unsigned char *ivec, const int enc);
		//dwRSA_free
		static void	netmodrf (RSA *r);
		//dwRSA_size
		static int	netmodrs(const RSA *);
		/* next 4 return -1 on error */
		//dwRSA_public_encrypt
		static int	netmodrpe(int flen, const unsigned char *from,
			unsigned char *to, RSA *rsa,int padding);
		//dwRSA_private_decrypt
		static int	netmodrpd(int flen, const unsigned char *from,
			unsigned char *to, RSA *rsa,int padding);
		//dwBIO_new
		static BIO *	netmodbn(BIO_METHOD *type);
		//dwBIO_s_mem
		static BIO_METHOD *netmodbsm(void);
		//dwBIO_write
		static int	netmodbw(BIO *b, const void *data, int len);
		//dwPEM_read_bio_RSAPrivateKey
		static RSA* netmodprbr(BIO *bp, RSA **x, pem_password_cb *cb, void *u);
		//dwBIO_free
		static int	netmodbf(BIO *a);
		//dwPEM_read_bio_RSA_PUBKEY
		static RSA* netmodprbrp(BIO *bp, RSA **x, pem_password_cb *cb, void *u);
		//dwCRYPTO_cleanup_all_ex_data
		static void netmodccaed(void);

		//improtocol
		//dwRAND_bytes
		static int  netmodrb(unsigned char *buf,int num);
		//dwRAND_seed
		static void netmodrs(const void *buf,int num);
		/* Deprecated version */
	#ifndef OPENSSL_NO_DEPRECATED
		//dwRSA_generate_key
		static RSA *	netmodrgk(int bits, unsigned long e,void
			(*callback)(int,int,void *),void *cb_arg);
	#endif /* !defined(OPENSSL_NO_DEPRECATED) */
		//dwRSA_check_key
		static int	netmodrck(const RSA *);
		//static void	dwRSA_free (RSA *r);
		//dwBN_bn2bin
		static int	netmodbb(const BIGNUM *a, unsigned char *to);
		//static int	dwRSA_private_decrypt(int flen, const unsigned char *from,
		//	unsigned char *to, RSA *rsa,int padding);
		//dwBF_set_key
		static void netmodbsk(BF_KEY *key, int len, const unsigned char *data);
		//dwBF_cbc_encrypt
		static void netmodbce(const unsigned char *in, unsigned char *out, long length,
			const BF_KEY *schedule, unsigned char *ivec, int enc);

		//netio
		//dwRC4_set_key
		static void netmodrsk(RC4_KEY *key, int len, const unsigned char *data);
		//dwRC4
		static void netmodr(RC4_KEY *key, size_t len, const unsigned char *indata,
			unsigned char *outdata);
		//dwMD5
		static unsigned char *netmodm2(const unsigned char *d, size_t n, unsigned char *md);

		static void netmodepf( EVP_PKEY *pkey );

		static unsigned char *netmodhmac(const EVP_MD *evp_md, const void *key, int key_len,
			const unsigned char *d, size_t n, unsigned char *md,
			unsigned int *md_len );

		static const EVP_MD * netmodesha1();

		static int netmodesf(EVP_MD_CTX *ctx,unsigned char *md,unsigned int *s,
			EVP_PKEY *pkey);

		static int netmodedu( EVP_MD_CTX *ctx,const void *d, size_t cnt );

		//EVP_DigestInit
		static int netmodedi( EVP_MD_CTX *ctx, const EVP_MD *type );

		static int netmodeps( EVP_PKEY *pkey );

		//CRYPTO_free
		static void netmodcf( void* );

		//BIO_new_mem_buf
		static BIO* netmodbnmf( void *buf, int len );

		//EVP_MD_CTX_cleanup
		static int netmodemcc( EVP_MD_CTX *ctx );

		//EVP_VerifyFinal
		static int netmodevf( EVP_MD_CTX *ctx,const unsigned char *sigbuf,
			unsigned int siglen,EVP_PKEY *pkey );

		//X509_get_pubkey
		static EVP_PKEY* netmodxgp(X509 *x);

		//EVP_MD_CTX_init
		static void netmodemci( EVP_MD_CTX *ctx );

		//EVP_DigestFinal
		static int netmodedf( EVP_MD_CTX *ctx,unsigned char *md,unsigned int *s );

		//EVP_MD_size
		static int netmodems( const EVP_MD *md );

		//NETMOD_PEM_read_bio_PrivateKey
		static EVP_PKEY *netmodprbp( BIO *bp, EVP_PKEY **x, pem_password_cb *cb, void *u );

		//PEM_read_bio_PUBKEY
		static EVP_PKEY *netmodprbpk( BIO *bp, EVP_PKEY **x, pem_password_cb *cb, void *u );

		//X509_free
		static void netmodxf(X509 *a);

		//PEM_read_bio_X509
		static X509 *netmodprbx(BIO *bp, X509 **x, pem_password_cb *cb, void *u);

		// int RSA_verify
		static int netmodrv(int type, const unsigned char *m, unsigned int m_length,
			const unsigned char *sigbuf, unsigned int siglen, RSA *rsa);


		//RSA *PEM_read_bio_RSAPublicKey
		static RSA *netmodprbrpk(BIO *bp, RSA **x, pem_password_cb *cb, void *u);
	};
}

#define NETMOD_EVP_Digest Netmod::BaseFunc::netmodedgt
#ifndef OPENSSL_NO_MD5
#define NETMOD_EVP_md5 Netmod::BaseFunc::netmodem
#endif
#define NETMOD_AES_set_encrypt_key Netmod::BaseFunc::netmodasek
#define NETMOD_AES_set_decrypt_key Netmod::BaseFunc::netmodasdk
#define NETMOD_AES_cbc_encrypt Netmod::BaseFunc::netmodace
#define NETMOD_RSA_free Netmod::BaseFunc::netmodrf
#define NETMOD_RSA_size Netmod::BaseFunc::netmodrs
#define NETMOD_RSA_public_encrypt Netmod::BaseFunc::netmodrpe
#define NETMOD_RSA_private_decrypt Netmod::BaseFunc::netmodrpd
#define NETMOD_BIO_new Netmod::BaseFunc::netmodbn
#define NETMOD_BIO_s_mem Netmod::BaseFunc::netmodbsm
#define NETMOD_BIO_write Netmod::BaseFunc::netmodbw
#define NETMOD_PEM_read_bio_RSAPrivateKey Netmod::BaseFunc::netmodprbr
#define NETMOD_BIO_free Netmod::BaseFunc::netmodbf
#define NETMOD_PEM_read_bio_RSA_PUBKEY Netmod::BaseFunc::netmodprbrp
#define NETMOD_CRYPTO_cleanup_all_ex_data Netmod::BaseFunc::netmodccaed
#define NETMOD_RAND_bytes Netmod::BaseFunc::netmodrb
#define NETMOD_RAND_seed Netmod::BaseFunc::netmodrs
#ifndef OPENSSL_NO_DEPRECATED
#define  NETMOD_RSA_generate_key Netmod::BaseFunc::netmodrgk
#endif /* !defined(OPENSSL_NO_DEPRECATED) */
#define NETMOD_RSA_check_key Netmod::BaseFunc::netmodrck
#define NETMOD_BN_bn2bin Netmod::BaseFunc::netmodbb
#define NETMOD_BF_set_key Netmod::BaseFunc::netmodbsk
#define NETMOD_BF_cbc_encrypt Netmod::BaseFunc::netmodbce
#define NETMOD_RC4_set_key Netmod::BaseFunc::netmodrsk
#define NETMOD_RC4 Netmod::BaseFunc::netmodr
#define NETMOD_MD5 Netmod::BaseFunc::netmodm2

#define NETMOD_EVP_PKEY_free  Netmod::BaseFunc::netmodepf
#define NETMOD_HMAC		Netmod::BaseFunc::netmodhmac
#define NETMOD_EVP_SHA1  Netmod::BaseFunc::netmodesha1
#define NETMOD_EVP_SignFinal  Netmod::BaseFunc::netmodesf
#define NETMOD_EVP_DigestUpdate  Netmod::BaseFunc::netmodedu
#define NETMOD_EVP_DigestInit	Netmod::BaseFunc::netmodedi
#define NETMOD_EVP_PKEY_size Netmod::BaseFunc::netmodeps
#define NETMOD_CRYPTO_free		Netmod::BaseFunc::netmodcf
#define NETMOD_BIO_new_mem_buf   Netmod::BaseFunc::netmodbnmf
#define NETMOD_EVP_MD_CTX_cleanup Netmod::BaseFunc::netmodemcc
#define NETMOD_EVP_VerifyFinal Netmod::BaseFunc::netmodevf
#define NETMOD_X509_get_pubkey  Netmod::BaseFunc::netmodxgp
#define NETMOD_EVP_MD_CTX_init  Netmod::BaseFunc::netmodemci
#define NETMOD_EVP_DigestFinal	Netmod::BaseFunc::netmodedf
#define NETMOD_EVP_MD_size  Netmod::BaseFunc::netmodems
#define NETMOD_PEM_read_bio_PrivateKey  Netmod::BaseFunc::netmodprbp
#define NETMOD_PEM_read_bio_PUBKEY	Netmod::BaseFunc::netmodprbpk
#define NETMOD_X509_free		Netmod::BaseFunc::netmodxf
#define NETMOD_PEM_read_bio_X509	Netmod::BaseFunc::netmodprbx
#define NETMOD_RSA_verify Netmod::BaseFunc::netmodrv
#define NETMOD_PEM_read_bio_RSAPublicKey Netmod::BaseFunc::netmodprbrpk


#endif //CLOUDWOOD_ANDROID_NEW_OPENSSL_PROXY_H
