// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: base.proto

package com.smart.android.smartandroid.loginsdk.proto;

public final class BaseProtoBean {
  private BaseProtoBean() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface BaseProtoMsgOrBuilder extends
      // @@protoc_insertion_point(interface_extends:base_event.BaseProtoMsg)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>int32 uri = 1;</code>
     */
    int getUri();
  }
  /**
   * Protobuf type {@code base_event.BaseProtoMsg}
   */
  public  static final class BaseProtoMsg extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:base_event.BaseProtoMsg)
      BaseProtoMsgOrBuilder {
    // Use BaseProtoMsg.newBuilder() to construct.
    private BaseProtoMsg(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private BaseProtoMsg() {
      uri_ = 0;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
    }
    private BaseProtoMsg(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      int mutable_bitField0_ = 0;
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!input.skipField(tag)) {
                done = true;
              }
              break;
            }
            case 8: {

              uri_ = input.readInt32();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.internal_static_base_event_BaseProtoMsg_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.internal_static_base_event_BaseProtoMsg_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg.class, com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg.Builder.class);
    }

    public static final int URI_FIELD_NUMBER = 1;
    private int uri_;
    /**
     * <code>int32 uri = 1;</code>
     */
    public int getUri() {
      return uri_;
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (uri_ != 0) {
        output.writeInt32(1, uri_);
      }
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (uri_ != 0) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(1, uri_);
      }
      memoizedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg)) {
        return super.equals(obj);
      }
      com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg other = (com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg) obj;

      boolean result = true;
      result = result && (getUri()
          == other.getUri());
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + URI_FIELD_NUMBER;
      hash = (53 * hash) + getUri();
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code base_event.BaseProtoMsg}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:base_event.BaseProtoMsg)
        com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsgOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.internal_static_base_event_BaseProtoMsg_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.internal_static_base_event_BaseProtoMsg_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg.class, com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg.Builder.class);
      }

      // Construct using com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        uri_ = 0;

        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.internal_static_base_event_BaseProtoMsg_descriptor;
      }

      public com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg getDefaultInstanceForType() {
        return com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg.getDefaultInstance();
      }

      public com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg build() {
        com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg buildPartial() {
        com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg result = new com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg(this);
        result.uri_ = uri_;
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg) {
          return mergeFrom((com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg other) {
        if (other == com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg.getDefaultInstance()) return this;
        if (other.getUri() != 0) {
          setUri(other.getUri());
        }
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private int uri_ ;
      /**
       * <code>int32 uri = 1;</code>
       */
      public int getUri() {
        return uri_;
      }
      /**
       * <code>int32 uri = 1;</code>
       */
      public Builder setUri(int value) {
        
        uri_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>int32 uri = 1;</code>
       */
      public Builder clearUri() {
        
        uri_ = 0;
        onChanged();
        return this;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return this;
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return this;
      }


      // @@protoc_insertion_point(builder_scope:base_event.BaseProtoMsg)
    }

    // @@protoc_insertion_point(class_scope:base_event.BaseProtoMsg)
    private static final com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg();
    }

    public static com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<BaseProtoMsg>
        PARSER = new com.google.protobuf.AbstractParser<BaseProtoMsg>() {
      public BaseProtoMsg parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new BaseProtoMsg(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<BaseProtoMsg> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<BaseProtoMsg> getParserForType() {
      return PARSER;
    }

    public com.smart.android.smartandroid.loginsdk.proto.BaseProtoBean.BaseProtoMsg getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_base_event_BaseProtoMsg_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_base_event_BaseProtoMsg_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\nbase.proto\022\nbase_event\"\033\n\014BaseProtoMsg" +
      "\022\013\n\003uri\030\001 \001(\005B>\n-com.smart.android.smart" +
      "android.loginsdk.protoB\rBaseProtoBeanb\006p" +
      "roto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_base_event_BaseProtoMsg_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_base_event_BaseProtoMsg_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_base_event_BaseProtoMsg_descriptor,
        new java.lang.String[] { "Uri", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
