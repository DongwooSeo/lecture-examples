package com.growmighty.lectures.firstday.product;

import com.growmighty.lectures.firstday.product.application.ProductService;
import com.growmighty.lectures.firstday.product.application.dto.RegisterProductCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class ProductDataInitializer implements CommandLineRunner {
    private final ProductService productService;

    @Override
    public void run(String... args) {
        register("삼성 노트북 갤럭시북4 프로", 2_150_000, 50, "가벼운 삼성 노트북입니다.");
        register("LG 그램 노트북 17인치", 1_890_000, 30, "초경량 대화면 노트북.");
        register("애플 맥북 에어 M3", 1_590_000, 40, "애플 실리콘 랩탑.");
        register("애플 맥북 프로 14 M3", 2_690_000, 20, "전문가용 laptop.");
        register("무선 기계식 키보드 청축", 120_000, 100, "타건감 좋은 무선 keyboard.");
        register("유선 기계식 키보드 적축", 90_000, 80, "사무실용 조용한 키보드.");
        register("로지텍 무선 마우스 MX Master 3S", 129_000, 200, "인체공학 wireless mouse.");
        register("게이밍 유선 마우스", 45_000, 150, "가성비 게이밍 마우스.");
        register("아이폰 15 프로 스마트폰", 1_550_000, 60, "티타늄 휴대폰.");
        register("갤럭시 S24 울트라 스마트폰", 1_690_000, 70, "AI 핸드폰.");
        register("무선 이어폰 버즈3 프로", 249_000, 300, "노이즈캔슬링 이어폰.");
        register("노트북 파우치 15인치", 25_000, 500, "노트북 보호 파우치.");

        register("새틴 슬립 드레스", 89_000, 40, "은은한 광택의 새틴 소재. 결혼식, 파티 등 격식 있는 자리에 어울리는 우아한 실루엣.");
        register("플라워 패턴 미디 원피스", 59_000, 60, "화사한 꽃무늬 봄 나들이용 미디 원피스.");
        register("셔츠형 롱 원피스", 64_000, 50, "단정한 카라의 데일리 셔츠 원피스.");
        register("캐주얼 데님 원피스", 49_000, 70, "부담 없이 입는 데님 소재 원피스.");
        register("발수 코팅 맥 코트", 159_000, 30, "발수 코팅 소재로 비 오는 날 출근길에도 안심되는 미니멀 맥 코트.");
        register("빈티지 워싱 데님 자켓", 79_000, 80, "사계절 데일리로 입기 좋은 워싱 데님 자켓.");
        register("트위드 세미 포멀 자켓", 129_000, 25, "격식 있는 자리와 오피스를 모두 소화하는 트위드 자켓.");
        register("링클프리 와이드 슬랙스", 55_000, 90, "구김 걱정 없는 출근용 와이드 슬랙스.");
        register("오버핏 옥스포드 셔츠", 45_000, 100, "꾸미지 않은 듯 자연스러운 데일리 셔츠.");
        register("린넨 와이드 팬츠", 52_000, 60, "여름 휴가지에서 시원하게 입는 린넨 팬츠.");
        register("시어서커 반팔 셔츠", 39_000, 80, "통풍이 잘 되는 여름 휴양지 셔츠.");
        register("캐시미어 블렌드 니트 가디건", 98_000, 40, "간절기 데일리로 걸치기 좋은 니트 가디건.");
        register("경량 패딩 베스트", 69_000, 50, "쌀쌀한 날 겹쳐 입는 경량 조끼.");
        register("방수 하이킹 바람막이", 89_000, 45, "우천 시에도 든든한 방수 아웃도어 바람막이.");
        register("플리츠 미디 스커트", 47_000, 55, "오피스와 격식 있는 자리에 두루 어울리는 주름 스커트.");
        register("블랙 앵클 첼시 부츠", 119_000, 35, "가을 데일리 룩을 완성하는 첼시 부츠.");
        register("스트랩 플랫 샌들", 42_000, 70, "여름 휴가용 스트랩 샌들.");
        register("울 블렌드 체스터 코트", 219_000, 20, "겨울 격식 코디의 기본이 되는 체스터 코트.");
        // 취향껏 몇 개 더 추가해도 좋습니다 (예: 모니터, 충전기, 케이스...)
    }

    private void register(String name, long price, int stock, String desc) {
        productService.register(
            new RegisterProductCommand(1L, name, BigDecimal.valueOf(price), stock, desc));
    }
}
