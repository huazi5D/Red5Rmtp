cmake_minimum_required(VERSION 3.4.1)

add_library( rtmp_push

             SHARED

             src/main/cpp/rtmp_handle.cpp )

include_directories(src/main/cpp/include)

find_library( log-lib

              log )

add_library(rtmp SHARED IMPORTED)
set_target_properties(rtmp PROPERTIES IMPORTED_LOCATION
  ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/librtmp.so)

target_link_libraries( rtmp_push

                       rtmp
                       ${log-lib} )