package cn.iocoder.financial.module.assets.service;

import cn.iocoder.financial.module.assets.dal.AssetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static cn.iocoder.financial.module.assets.service.AssetValues.flag;
import static cn.iocoder.financial.module.assets.service.AssetValues.forbidden;
import static cn.iocoder.financial.module.assets.service.AssetValues.id;
import static cn.iocoder.financial.module.assets.service.AssetValues.invalid;
import static cn.iocoder.financial.module.assets.service.AssetValues.userId;

@Service
public class AssetAccessService {
    private final AssetRepository repository;

    public AssetAccessService(AssetRepository repository) {
        this.repository = repository;
    }

    public record Scope(Map<String, Object> family, long familyId, long memberId, boolean owner,
                         List<Map<String, Object>> members, List<Long> visibleIds) {
    }

    public Scope scope(boolean lock) {
        Map<String, Object> family = repository.selectFamily(Map.of("userId", userId(), "lock", lock));
        if (family == null) {
            throw invalid("请先建立家庭账本，或请家庭所有者将你的系统用户编号关联到成员");
        }
        long familyId = id(family, "id");
        long memberId = id(family, "currentMemberId");
        boolean owner = id(family, "ownerUserId") == userId();
        List<Map<String, Object>> members = repository.selectMembers(Map.of("familyId", familyId));
        List<Long> visible = members.stream().map(member -> id(member, "id"))
                .filter(member -> owner || flag(family.get("allowViewOthers")) || member == memberId).toList();
        return new Scope(family, familyId, memberId, owner, members, visible);
    }

    public List<Long> visible(Scope scope, Long memberId) {
        if (memberId == null) {
            return scope.visibleIds();
        }
        if (!scope.visibleIds().contains(memberId)) {
            throw forbidden();
        }
        return List.of(memberId);
    }

    public void editable(Scope scope, long memberId) {
        if (scope.members().stream().noneMatch(member -> id(member, "id") == memberId)
                || !scope.owner() && scope.memberId() != memberId) {
            throw forbidden();
        }
    }

    public void owner(Scope scope) {
        if (!scope.owner()) {
            throw forbidden();
        }
    }
}
