cmake_minimum_required(VERSION 3.10.2)
project("llm_agent_jni")

# Define where external libs are located (Stub paths)
set(NCNN_DIR ${CMAKE_SOURCE_DIR}/libs/ncnn)
set(ONNX_DIR ${CMAKE_SOURCE_DIR}/libs/onnxruntime)

# Include directories
include_directories(${CMAKE_SOURCE_DIR}/include)

# Stubbed include for now to allow "compilation" in theory
include_directories(${CMAKE_SOURCE_DIR}/include/ncnn)
include_directories(${CMAKE_SOURCE_DIR}/include/onnxruntime)

add_library(llm_agent_jni SHARED
            llm_agent_jni.cpp
            ModelEngine.cpp
            NcnnEngine.cpp
            OnnxEngine.cpp)

# Link libraries (commented out as actual libs are missing)
# target_link_libraries(llm_agent_jni android log ncnn onnxruntime)
target_link_libraries(llm_agent_jni android log)
