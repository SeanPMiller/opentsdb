package net.opentsdb.query.processor.expressions2;

import net.opentsdb.data.types.numeric.NumericType;
import net.opentsdb.query.QueryFillPolicy;
import net.opentsdb.query.interpolation.types.numeric.NumericInterpolatorConfig;
import net.opentsdb.query.joins.JoinConfig;
import net.opentsdb.query.pojo.FillPolicy;

public class ConfigBasedTest {
    protected final NumericInterpolatorConfig NUMERIC_CONFIG;

    protected final JoinConfig JOIN_CONFIG;

    public ConfigBasedTest() {
        NUMERIC_CONFIG =
                (NumericInterpolatorConfig) NumericInterpolatorConfig.newBuilder()
                        .setFillPolicy(FillPolicy.NOT_A_NUMBER)
                        .setRealFillPolicy(QueryFillPolicy.FillWithRealPolicy.PREFER_NEXT)
                        .setDataType(NumericType.TYPE.toString())
                        .build();

        JOIN_CONFIG = (JoinConfig) JoinConfig.newBuilder()
                .setJoinType(JoinConfig.JoinType.INNER)
                .addJoins("host", "host")
                .setId("join")
                .setId("expression")
                .build();
    }
}
