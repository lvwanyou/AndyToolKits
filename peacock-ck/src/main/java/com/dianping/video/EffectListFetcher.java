package com.dianping.video;

import com.cutsame.solution.source.effect.EffectListRequest;
import com.cutsame.solution.source.effect.EffectListResponse;
import com.cutsame.solution.source.effect.IEffectListFetcher;
import com.dianping.codelog.NovaCodeLog;
import com.dianping.video.bean.EffectResources;
import com.dianping.video.bean.EffectResponse;
import com.dianping.video.utils.CKHelper;
import com.dianping.video.utils.Const;
import com.google.gson.Gson;


public class EffectListFetcher implements IEffectListFetcher {

    private static final String TAG = "EffectListFetcher";

    @Override
    public EffectListResponse fetchEffectListWithParams(EffectListRequest effectListRequest) {
        CKHelper.novaCodeLogI(EffectListFetcher.class, TAG, "panel: " + effectListRequest.getPanel());
        CKHelper.novaCodeLogI(EffectListFetcher.class, TAG, "resourceIDs: " + effectListRequest.getResourceIds().toString());

        NovaCodeLog.i(EffectListFetcher.class, TAG, "panel: " + effectListRequest.getPanel());
        NovaCodeLog.i(EffectListFetcher.class, TAG, "resourceIDs: " + effectListRequest.getResourceIds().toString());

        EffectResponse effectResponse = new EffectResponse();
        if (Const.effectResources != null && Const.effectResources.getEffect_resource_list() != null) {
            for (int i = 0; i < Const.effectResources.getEffect_resource_list().size(); i++) {
                // panel 匹配
                if (effectListRequest.getPanel().equals(Const.effectResources.getEffect_resource_list().get(i).getPanel())) {
                    for (int j = 0; j < effectListRequest.getResourceIds().size(); j++) {
                        for (int k = 0; k < Const.effectResources.getEffect_resource_list().get(i).getEffect_infos().size(); k++) {
                            // resourceId 匹配
                            if (effectListRequest.getResourceIds().get(j).equals(Const.effectResources.getEffect_resource_list().get(i).getEffect_infos().get(k).getResource_id())) {
                                EffectResources.EffectResourceListDTO.EffectInfosDTO effectInfosDTO = Const.effectResources.getEffect_resource_list().get(i).getEffect_infos().get(k);

                                EffectResponse.DataDTO.EffectListDTO effectListDTO = new EffectResponse.DataDTO.EffectListDTO();
                                effectListDTO.setId(effectInfosDTO.getId());
                                effectListDTO.setResource_id(effectInfosDTO.getResource_id());
                                effectListDTO.setName(effectInfosDTO.getName());
                                effectListDTO.setUri(effectInfosDTO.getUri());
                                effectListDTO.setUrl_list(effectInfosDTO.getUrl_list());
                                effectListDTO.setNeed_unzip(effectInfosDTO.getNeed_unzip());
                                effectListDTO.setModel_names(effectInfosDTO.getModel_names());
                                effectListDTO.setRequirements(effectInfosDTO.getRequirements());

                                effectResponse.getData().getEffect_list().add(effectListDTO);
                            }
                        }
                    }
                }
            }
        }

        if (effectResponse.getData().getEffect_list().size() == 0) {
            return new EffectListResponse(true, "{}");
        }

        return new EffectListResponse(true, new Gson().toJson(effectResponse));
    }
}
