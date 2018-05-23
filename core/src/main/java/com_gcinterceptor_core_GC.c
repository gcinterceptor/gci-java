#include "jni.h"
#include "jvmti.h"
#include "com_gcinterceptor_core_GC.h"

/* Check for JVMTI error */
#define CHECK_JVMTI_ERROR(err) \
checkJvmtiError(err, __FILE__, __LINE__)

static jvmtiEnv *jvmti;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
	jvmti = NULL;
	jint rc = (*vm)->GetEnv(vm, (void **)&jvmti, JVMTI_VERSION);
	if (rc != JNI_OK) {
		fprintf(stderr, "ERROR: Unable to create jvmtiEnv, GetEnv failed, error=%d\n", rc);
		return -1;
	}
	if (!jvmti) {
		fprintf(stderr, "jvmti is null");
		return -1;
		
	}
	return JNI_VERSION_10;
}

JNIEXPORT void JNICALL Java_com_gcinterceptor_core_GC_force(JNIEnv *env, jclass js) {
	(*jvmti)->ForceGarbageCollection(jvmti);
}
