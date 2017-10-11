//
// Created by System Administrator on 16/8/30.
//

#ifndef CLOUDWOOD_ANDROID_NEW_STRING_UTIL_H
#define CLOUDWOOD_ANDROID_NEW_STRING_UTIL_H

#include <sstream>
#include <ios>
#include <type_traits>

namespace string_util{
	template<typename T>
	inline std::wstring to_wstring(T& t)
	{
		std::wstringstream wss;
		wss << t;
		return wss.str();
	}

	template<typename T>
	inline std::string to_string(T& t)
	{
		std::stringstream ss;
		ss << t;
		return ss.str();
	}

	template<typename T>
	T str_to_num(const char *s, const T& val = T())
    {
        static_assert(std::is_integral<T>::value || std::is_floating_point<T>::value, "Integer or Float/Double is required.");
        T num(val);
        if(s != nullptr) {
            std::stringstream ss(s);
            ss >> std::dec >> num;
        }
        return num;
    }
}

#endif //CLOUDWOOD_ANDROID_NEW_STRING_UTIL_H
