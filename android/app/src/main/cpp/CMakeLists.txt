cmake_minimum_required(VERSION 3.10)

# Include the main CMakeLists.txt
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/../../../native/cpp spade_ace_native)

# Android-specific configurations
if(ANDROID)
    # Find required Android libraries
    find_library(log-lib log)
    
    # Link Android-specific libraries
    target_link_libraries(spade_ace_native
        ${log-lib}
    )
endif()