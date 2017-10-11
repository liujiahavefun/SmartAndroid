//
// Created by liujia on 16/8/21.
//

#ifndef CLOUDWOOD_ANDROID_NEW_VARSTR_H
#define CLOUDWOOD_ANDROID_NEW_VARSTR_H

#include <iostream>

namespace sox {

struct Varstr {
	const char * m_data;
	size_t m_size;

	Varstr(const char * data = "", size_t size = 0) {
		set(data, size);
	}
	void set(const char * data, size_t size) {
		m_data = data;
		m_size = size;
	}
	bool empty() const {
		return m_size == 0;
	}

	const char * data() const {
		return m_data;
	}
	size_t size() const {
		return m_size;
	}

	template<class T> // std::string cstr blockbuffer
	explicit Varstr(T & s) {
		*this = s;
	}

	template<class T> Varstr& operator =(T & s) {
		m_data = s.data();
		m_size = s.size();
		return *this;
	}
};

inline std::ostream & operator <<(std::ostream & os, const Varstr & vs) {
	return os.write(vs.data(), std::streamsize(vs.size()));
}

} // namespace sox

#endif //CLOUDWOOD_ANDROID_NEW_VARSTR_H
