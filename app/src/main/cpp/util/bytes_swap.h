//
// Created by liujia on 16/8/21.
//

#ifndef SMART_ANDROID_NEW_BYTES_SWAP_H
#define SMART_ANDROID_NEW_BYTES_SWAP_H

static bool is_little_endian()
{
    uint32_t i = 0x12345678;
    uint16_t *s = (uint16_t*)&i;
    return !(0x1234 == s[0]);
}

static inline uint16_t bswap_16(uint16_t val)
{
    return ((val & 0xff) << 8) | ((val >> 8) & 0xff);
}

static inline uint32_t bswap_32(uint32_t val)
{
    return bswap_16((uint16_t)val) << 16 |
           bswap_16((uint16_t)(val >> 16));
}

static inline uint64_t bswap_64(uint64_t val)
{
    return ((((uint64_t)bswap_32(val)) << 32) |
            (((uint64_t)bswap_32(val >> 32)) & 0xffffffffULL));
}

template <typename T>
T to_little_endian(T v);

template <>
inline uint16_t to_little_endian(uint16_t v) {
    if(is_little_endian()) {
        return v;
    }
    return bswap_16(v);
}

template <>
inline uint32_t to_little_endian(uint32_t v) {
    if(is_little_endian()) {
        return v;
    }
    return bswap_32(v);
}

template <>
inline uint64_t to_little_endian(uint64_t v) {
    if(is_little_endian()) {
        return v;
    }
    return bswap_64(v);
}

#endif //SMART_ANDROID_NEW_BYTES_SWAP_H
