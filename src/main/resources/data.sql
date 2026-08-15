-- =================================================================================
-- AKA SYSTEM - FILE NẠP CSDL TỰ ĐỘNG CHUẨN SPRING BOOT (DATA.SQL)
-- Môi trường: Spring Boot Native SQL Initialization
-- =================================================================================

-- 1. BẢNG TRƯỜNG MẦM NON
IF OBJECT_ID('dbo.Schools', 'U') IS NOT NULL
BEGIN
    IF (SELECT COUNT(*) FROM dbo.Schools) = 0
    BEGIN
        SET IDENTITY_INSERT dbo.Schools ON;
        INSERT INTO dbo.Schools (id, name, address, contactPerson, phone) VALUES
        (1, N'Trường Mầm Non Tuệ Đức',         N'123 Lương Định Của, TP. Thủ Đức', N'Thầy Đức',   '0956789013'),
        (2, N'Trường Mầm Non Họa Mi',          N'404 Phan Xích Long, Phú Nhuận',  N'Cô Hương',   '0977889900'),
        (3, N'Trường Mầm Non Tuổi Thần Tiên',  N'101 Cộng Hòa, Tân Bình',        N'Cô Minh',    '0944556677'),
        (4, N'Trường Mầm Non Hoa Mai',         N'123 Nguyễn Huệ, Quận 1',         N'Cô Hoa',     '0911223344');
        SET IDENTITY_INSERT dbo.Schools OFF;
    END
END
GO

-- 2. BẢNG LỚP HỌC
IF OBJECT_ID('dbo.Classes', 'U') IS NOT NULL
BEGIN
    IF (SELECT COUNT(*) FROM dbo.Classes) = 0
    BEGIN
        SET IDENTITY_INSERT dbo.Classes ON;
        INSERT INTO dbo.Classes (id, name, schoolId, studentCount, standardPeriods) VALUES
        (1, N'Lớp Aerobic Lá 4',  1, 25, 2),
        (2, N'Lớp Aerobic Lá 3',  2, 23, 2),
        (3, N'Lớp Aerobic Lá 1',  3, 24, 2),
        (4, N'Lớp Aerobic Chồi 1', 4, 20, 2);
        SET IDENTITY_INSERT dbo.Classes OFF;
    END
END
GO

-- 3. BẢNG GIÁO VIÊN
IF OBJECT_ID('dbo.Teachers', 'U') IS NOT NULL
BEGIN
    IF (SELECT COUNT(*) FROM dbo.Teachers) = 0
    BEGIN
        SET IDENTITY_INSERT dbo.Teachers ON;
        INSERT INTO dbo.Teachers (id, name, phone, email, status) VALUES
        (1, N'Lê Thị Kim Anh',   '0977888999', 'kimanhle@gmail.com', 'active'),
        (2, N'Phạm Thanh Thảo',  '0905111222', 'thaopham@gmail.com', 'active'),
        (3, N'Trần Minh Hoàng',  '0987654321', 'hoangtran@gmail.com', 'active'),
        (4, N'Vũ Hoàng Long',   '0944556666', 'admin.long@gmail.com', 'active'),
        (5, N'Đặng Hồng Hạnh',  '0933444555', 'both.hanh@gmail.com', 'active');
        SET IDENTITY_INSERT dbo.Teachers OFF;
    END
END
GO

-- 4. BẢNG TÀI KHOẢN NGƯỜI DÙNG USERS (MẬT KHẨU MẶC ĐỊNH LÀ 123456)
IF OBJECT_ID('dbo.Users', 'U') IS NOT NULL
BEGIN
    IF (SELECT COUNT(*) FROM dbo.Users) = 0
    BEGIN
        SET IDENTITY_INSERT dbo.Users ON;
        INSERT INTO dbo.Users (id, username, password, role, enabled, teacherId) VALUES
        (1, 'admin.long@gmail.com',     '$2a$10$e7.S9S234.SgqYc/kM66X.vN3fKz6w1K.wW/tM8jN9h.n8P1t8o3m', 'ROLE_ADMIN', 1, 4),
        (2, 'both.hanh@gmail.com',      '$2a$10$e7.S9S234.SgqYc/kM66X.vN3fKz6w1K.wW/tM8jN9h.n8P1t8o3m', 'ROLE_ADMIN', 1, 5),
        (3, 'admin.trungtam@gmail.com', '$2a$10$e7.S9S234.SgqYc/kM66X.vN3fKz6w1K.wW/tM8jN9h.n8P1t8o3m', 'ROLE_ADMIN', 1, NULL),
        (4, 'canhthang457@gmail.com',   '$2a$10$e7.S9S234.SgqYc/kM66X.vN3fKz6w1K.wW/tM8jN9h.n8P1t8o3m', 'ROLE_TEACHER', 1, 1),
        (5, 'teacher.thao@gmail.com',   '$2a$10$e7.S9S234.SgqYc/kM66X.vN3fKz6w1K.wW/tM8jN9h.n8P1t8o3m', 'ROLE_TEACHER', 1, 2),
        (6, 'teacher.hoang@gmail.com',  '$2a$10$e7.S9S234.SgqYc/kM66X.vN3fKz6w1K.wW/tM8jN9h.n8P1t8o3m', 'ROLE_TEACHER', 1, 3);
        SET IDENTITY_INSERT dbo.Users OFF;
    END
END
GO
