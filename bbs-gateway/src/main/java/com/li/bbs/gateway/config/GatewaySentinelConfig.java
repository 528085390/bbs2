package com.li.bbs.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class GatewaySentinelConfig {

    @PostConstruct
    public void init() {
        initApiGroups();
        initGatewayRules();
    }

    private void initApiGroups() {
        Set<ApiDefinition> definitions = Set.of(
                new ApiDefinition("auth_api")
                        .setPredicateItems(Set.of(
                                new ApiPathPredicateItem().setPattern("/api/auth/**")
                                        .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)
                        )),
                new ApiDefinition("post_api")
                        .setPredicateItems(Set.of(
                                new ApiPathPredicateItem().setPattern("/api/posts/**")
                                        .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)
                        )),
                new ApiDefinition("search_api")
                        .setPredicateItems(Set.of(
                                new ApiPathPredicateItem().setPattern("/api/search/**")
                                        .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)
                        ))
        );
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
    }

    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = Set.of(
                new GatewayFlowRule("auth_api")
                        .setCount(20)
                        .setIntervalSec(1),
                new GatewayFlowRule("post_api")
                        .setCount(50)
                        .setIntervalSec(1),
                new GatewayFlowRule("search_api")
                        .setCount(20)
                        .setIntervalSec(1)
        );
        GatewayRuleManager.loadRules(rules);
    }
}
