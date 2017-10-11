//
// Created by liujia on 16/8/20.
//


#include "openssl_proxy.h"
#include "locker/rclocker.h"
#include <assert.h>

const char rnd_seed[] = "string to make the random number generator think it has entropy";

namespace Netmod
{
	RSA *g_rsaKey = NULL;
	volatile LONG g_rsaKeyRef = 0;
	DwCSLock g_csRsaKey;

	int BaseFunc::netmodedgt( const void *data, size_t count, unsigned char *md, unsigned int *size, const EVP_MD *type, ENGINE *impl )
	{
		return EVP_Digest(data, count, md, size, type, impl);
	}

	#ifndef OPENSSL_NO_MD5
	const EVP_MD * BaseFunc::netmodem( void )
	{
		return EVP_md5();
	}
	#endif

	int BaseFunc::netmodasek( const unsigned char *userKey, const int bits, AES_KEY *key)
	{
		return AES_set_encrypt_key(userKey, bits, key);
	}

	int BaseFunc::netmodasdk( const unsigned char *userKey, const int bits, AES_KEY *key)
	{
		return AES_set_decrypt_key(userKey, bits, key);
	}

	void BaseFunc::netmodace( const unsigned char *in, unsigned char *out, size_t length, const AES_KEY *key, unsigned char *ivec, const int enc)
	{
		AES_cbc_encrypt(in, out, length, key, ivec, enc);
	}

	void BaseFunc::netmodrf( RSA *r)
	{
		assert(r != NULL);

		NETMOD_AUTO_LOCKER(&g_csRsaKey);

		if(r == g_rsaKey)
		{
			if(--g_rsaKeyRef == 0)
			{
				RSA_free(g_rsaKey);
				g_rsaKey = NULL;
			}
		}
		else
		{
			RSA_free(r);
		}
	}

	int BaseFunc::netmodrs( const RSA *r)
	{
		assert(r != NULL);

		return RSA_size(r);
	}

	int BaseFunc::netmodrpe( int flen, const unsigned char *from, unsigned char *to, RSA *rsa,int padding)
	{
		return RSA_public_encrypt(flen, from, to, rsa, padding);
	}

	int BaseFunc::netmodrpd( int flen, const unsigned char *from, unsigned char *to, RSA *rsa,int padding)
	{
		return RSA_private_decrypt(flen, from, to, rsa, padding);
	}

	BIO * BaseFunc::netmodbn( BIO_METHOD *type)
	{
		return BIO_new(type);
	}

	BIO_METHOD * BaseFunc::netmodbsm( void )
	{
		return BIO_s_mem();
	}

	int BaseFunc::netmodbw( BIO *b, const void *data, int len)
	{
		return BIO_write(b, data, len);
	}

	RSA* BaseFunc::netmodprbr( BIO *bp, RSA **x, pem_password_cb *cb, void *u)
	{
		return PEM_read_bio_RSAPrivateKey(bp, x, cb, u);
	}

	int BaseFunc::netmodbf( BIO *a)
	{
		return BIO_free(a);
	}

	RSA* BaseFunc::netmodprbrp( BIO *bp, RSA **x, pem_password_cb *cb, void *u)
	{
		return PEM_read_bio_RSA_PUBKEY(bp, x, cb, u);
	}

	void BaseFunc::netmodccaed( void )
	{
		CRYPTO_cleanup_all_ex_data();
	}

	int BaseFunc::netmodrb( unsigned char *buf,int num)
	{
		return RAND_bytes(buf, num);
	}

	void BaseFunc::netmodrs( const void *buf,int num)
	{
		RAND_seed(buf, num);
	}

	#ifndef OPENSSL_NO_DEPRECATED
	RSA * BaseFunc::netmodrgk( int bits, unsigned long e,void (*callback)(int,int,void *),void *cb_arg)
	{

		NETMOD_AUTO_LOCKER(&g_csRsaKey);

		if(bits == 0 && e == 0 && callback == NULL && cb_arg == NULL)
		{
			if(g_rsaKeyRef == 0)
			{

				NETMOD_RAND_seed(rnd_seed, sizeof rnd_seed);

				g_rsaKey =  RSA_generate_key(512, 3, NULL, NULL);
				while(RSA_check_key(g_rsaKey) != 1)
				{
					RSA_free(g_rsaKey);
					g_rsaKey = RSA_generate_key(512, 3, NULL, NULL);
				}
			}
			g_rsaKeyRef++;
			return g_rsaKey;
		}
		else
		{
			return RSA_generate_key(bits, e, callback, cb_arg);
		}
	}
	#endif

	int BaseFunc::netmodrck( const RSA * r)
	{
		return 	RSA_check_key(r);
	}

	int BaseFunc::netmodbb( const BIGNUM *a, unsigned char *to)
	{
		return BN_bn2bin(a, to);
	}

	void BaseFunc::netmodbsk( BF_KEY *key, int len, const unsigned char *data)
	{
		BF_set_key(key, len, data);
	}

	void BaseFunc::netmodbce( const unsigned char *in, unsigned char *out, long length, const BF_KEY *schedule, unsigned char *ivec, int enc)
	{
		BF_cbc_encrypt(in, out, length, schedule, ivec, enc);
	}

	void BaseFunc::netmodrsk( RC4_KEY *key, int len, const unsigned char *data)
	{
		RC4_set_key(key, len, data);
	}

	void BaseFunc::netmodr( RC4_KEY *key, size_t len, const unsigned char *indata, unsigned char *outdata)
	{
		RC4(key, len, indata, outdata);
	}

	unsigned char * BaseFunc::netmodm2( const unsigned char *d, size_t n, unsigned char *md)
	{
		return MD5(d, n, md);
	}

	void BaseFunc::netmodepf( EVP_PKEY *pkey )
	{
		EVP_PKEY_free( pkey );
	}

	unsigned char* BaseFunc::netmodhmac(const EVP_MD *evp_md, const void *key, int key_len,
						  const unsigned char *d, size_t n, unsigned char *md,
						  unsigned int *md_len )
	{
		return HMAC(evp_md, key, key_len, d, n, md, md_len );
	}

	const EVP_MD * BaseFunc::netmodesha1()
	{
		return EVP_sha1();
	}

	int BaseFunc::netmodesf(EVP_MD_CTX *ctx,unsigned char *md,unsigned int *s,
					 EVP_PKEY *pkey)
	{
		return EVP_SignFinal( ctx, md, s,  pkey );
	}

	int BaseFunc::netmodedu( EVP_MD_CTX *ctx,const void *d, size_t cnt )
	{
		return	EVP_DigestUpdate( ctx, d, cnt );
	}

	int BaseFunc::netmodedi( EVP_MD_CTX *ctx, const EVP_MD *type )
	{
		return EVP_DigestInit( ctx, type );
	}

	int	BaseFunc::netmodeps( EVP_PKEY *pkey )
	{
		return EVP_PKEY_size( pkey );
	}

	void BaseFunc::netmodcf( void* addr )
	{
		CRYPTO_free( addr );
	}

	BIO* BaseFunc::netmodbnmf( void *buf, int len )
	{
		return BIO_new_mem_buf( buf, len );
	}

	int BaseFunc::netmodemcc( EVP_MD_CTX *ctx )
	{
		return EVP_MD_CTX_cleanup( ctx );
	}

	int BaseFunc::netmodevf( EVP_MD_CTX *ctx,const unsigned char *sigbuf,
					 unsigned int siglen,EVP_PKEY *pkey )
	{
		return EVP_VerifyFinal( ctx, sigbuf, siglen, pkey );
	}

	EVP_PKEY* BaseFunc::netmodxgp(X509 *x)
	{
		return X509_get_pubkey( x );
	}

	void BaseFunc::netmodemci( EVP_MD_CTX *ctx )
	{
		return EVP_MD_CTX_init( ctx );
	}

	int BaseFunc::netmodedf( EVP_MD_CTX *ctx,unsigned char *md,unsigned int *s )
	{
		return EVP_DigestFinal( ctx, md, s );
	}

	int BaseFunc::netmodems( const EVP_MD *md )
	{
		return EVP_MD_size( md );
	}

	EVP_PKEY *BaseFunc::netmodprbp( BIO *bp, EVP_PKEY **x, pem_password_cb *cb, void *u )
	{
		return PEM_read_bio_PrivateKey( bp, x, cb, u );
	}

	EVP_PKEY *BaseFunc::netmodprbpk( BIO *bp, EVP_PKEY **x, pem_password_cb *cb, void *u )
	{
		return PEM_read_bio_PUBKEY( bp, x, cb, u );
	}

	void BaseFunc::netmodxf(X509 *a)
	{
		X509_free(a);
	}

	X509* BaseFunc::netmodprbx(BIO *bp, X509 **x, pem_password_cb *cb, void *u)
	{
		return PEM_read_bio_X509(bp, x, cb, u);
	}

	int BaseFunc::netmodrv(int type, const unsigned char *m, unsigned int m_length,
			const unsigned char *sigbuf, unsigned int siglen, RSA *rsa)
	{
		return RSA_verify(type, m, m_length, sigbuf, siglen, rsa);
	}

	RSA *BaseFunc::netmodprbrpk(BIO *bp, RSA **x, pem_password_cb *cb, void *u)
	{
		return PEM_read_bio_RSAPublicKey(bp, x, cb ,u);
	}
}
