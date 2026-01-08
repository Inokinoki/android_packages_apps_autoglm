#include "ModelEngine.h"
#include "NcnnEngine.h"
#include "OnnxEngine.h"

ModelEngine* ModelEngine::create(EngineType type) {
    if (type == EngineType::NCNN) {
        return new NcnnEngine();
    } else if (type == EngineType::ONNX) {
        return new OnnxEngine();
    }
    return nullptr;
}
