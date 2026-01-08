cmake_minimum_required(VERSION 3.10)
project(test_jni_build)

include_directories(${CMAKE_SOURCE_DIR}/mock_includes)
include_directories(${CMAKE_SOURCE_DIR}/src/jni)

# Stub NCNN/ONNX includes to avoid errors in source
file(WRITE ${CMAKE_SOURCE_DIR}/mock_includes/ncnn/net.h "// stub")
file(WRITE ${CMAKE_SOURCE_DIR}/mock_includes/onnxruntime_cxx_api.h "// stub")
include_directories(${CMAKE_SOURCE_DIR}/mock_includes/ncnn)
include_directories(${CMAKE_SOURCE_DIR}/mock_includes/onnxruntime)

add_library(llm_agent_jni SHARED
    src/jni/llm_agent_jni.cpp
    src/jni/ModelEngine.cpp
    src/jni/NcnnEngine.cpp
    src/jni/OnnxEngine.cpp
)

# No linking against android/log for host build, as we stubbed the header and implementation is inline or missing
