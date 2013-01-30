LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := AGG

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
AGG_SRC := src
					
LOCAL_SRC_FILES := $(AGG_SRC)/agg_arc.cpp \
$(AGG_SRC)/agg_arrowhead.cpp \
$(AGG_SRC)/agg_bezier_arc.cpp \
$(AGG_SRC)/agg_bspline.cpp \
$(AGG_SRC)/agg_curves.cpp \
$(AGG_SRC)/agg_vcgen_contour.cpp \
$(AGG_SRC)/agg_vcgen_dash.cpp \
$(AGG_SRC)/agg_vcgen_markers_term.cpp \
$(AGG_SRC)/agg_vcgen_smooth_poly1.cpp \
$(AGG_SRC)/agg_vcgen_stroke.cpp \
$(AGG_SRC)/agg_vcgen_bspline.cpp \
$(AGG_SRC)/agg_gsv_text.cpp \
$(AGG_SRC)/agg_image_filters.cpp \
$(AGG_SRC)/agg_line_aa_basics.cpp \
$(AGG_SRC)/agg_line_profile_aa.cpp \
$(AGG_SRC)/agg_rounded_rect.cpp \
$(AGG_SRC)/agg_sqrt_tables.cpp \
$(AGG_SRC)/agg_embedded_raster_fonts.cpp \
$(AGG_SRC)/agg_trans_affine.cpp \
$(AGG_SRC)/agg_trans_warp_magnifier.cpp \
$(AGG_SRC)/agg_trans_single_path.cpp \
$(AGG_SRC)/agg_trans_double_path.cpp \
$(AGG_SRC)/agg_vpgen_clip_polygon.cpp \
$(AGG_SRC)/agg_vpgen_clip_polyline.cpp \
$(AGG_SRC)/agg_vpgen_segmentator.cpp \
$(AGG_SRC)/ctrl/agg_cbox_ctrl.cpp \
$(AGG_SRC)/ctrl/agg_gamma_ctrl.cpp \
$(AGG_SRC)/ctrl/agg_gamma_spline.cpp \
$(AGG_SRC)/ctrl/agg_rbox_ctrl.cpp \
$(AGG_SRC)/ctrl/agg_slider_ctrl.cpp \
$(AGG_SRC)/ctrl/agg_spline_ctrl.cpp \
$(AGG_SRC)/ctrl/agg_scale_ctrl.cpp \
$(AGG_SRC)/ctrl/agg_polygon_ctrl.cpp \
$(AGG_SRC)/ctrl/agg_bezier_ctrl.cpp

LOCAL_CFLAGS := -O3


include $(BUILD_STATIC_LIBRARY)