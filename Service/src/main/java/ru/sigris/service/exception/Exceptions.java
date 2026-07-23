package ru.sigris.service.exception;

public class Exceptions {
    public static class PipelineNotFoundException extends RuntimeException {
        public PipelineNotFoundException() {
            super("Pipeline not found");
        }
    }

    public static class NodeNotFoundException extends RuntimeException {
        public NodeNotFoundException() {
            super("Node not found");
        }
    }

    public static class PipelineExist extends RuntimeException {
        public PipelineExist() {
            super("Pipeline already exist");
        }
    }

    public static class NodeExistInPipeline extends RuntimeException {
        public NodeExistInPipeline() {
            super("Node already exist in pipeline");
        }
    }

    public static class PipelineHasntNode extends RuntimeException {
        public PipelineHasntNode() {
            super("Pipeline has not node");
        }
    }

    public static class EdgeExist extends RuntimeException {
        public EdgeExist() {
            super("Edge already exist");
        }
    }
}
