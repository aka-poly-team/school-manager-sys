-- =================================================================================
-- AKA SYSTEM DATABASE CREATION & SEED DATA SCRIPT (SQL SERVER / AZURE SQL)
-- Môi trường: SQL Server 2016+ / Azure SQL Database / LocalDB
-- Tác giả: AKA Team
-- Phiên bản: 2.0 (Đồng bộ hoàn toàn với các Entity Java Spring Boot)
-- =================================================================================

USE master;
GO

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'aka')
BEGIN
    CREATE DATABASE aka;
END
GO

USE aka;
GO

-- =============================================
-- 1. DỌN DẸP BẢNG CŨ NẾU ĐÃ TỒN TẠI (DROP TABLES)
-- =============================================
IF OBJECT_ID('dbo.PasswordResetTokens', 'U') IS NOT NULL DROP TABLE dbo.PasswordResetTokens;
IF OBJECT_ID('dbo.DocumentTemplates', 'U') IS NOT NULL DROP TABLE dbo.DocumentTemplates;
IF OBJECT_ID('dbo.SystemLogs', 'U')        IS NOT NULL DROP TABLE dbo.SystemLogs;
IF OBJECT_ID('dbo.Notifications', 'U')     IS NOT NULL DROP TABLE dbo.Notifications;
IF OBJECT_ID('dbo.Complaints', 'U')        IS NOT NULL DROP TABLE dbo.Complaints;
IF OBJECT_ID('dbo.ChangeRequests', 'U')    IS NOT NULL DROP TABLE dbo.ChangeRequests;
IF OBJECT_ID('dbo.Attendance', 'U')        IS NOT NULL DROP TABLE dbo.Attendance;
IF OBJECT_ID('dbo.Schedules', 'U')         IS NOT NULL DROP TABLE dbo.Schedules;
IF OBJECT_ID('dbo.Classes', 'U')           IS NOT NULL DROP TABLE dbo.Classes;
IF OBJECT_ID('dbo.Schools', 'U')           IS NOT NULL DROP TABLE dbo.Schools;
IF OBJECT_ID('dbo.Users', 'U')             IS NOT NULL DROP TABLE dbo.Users;
IF OBJECT_ID('dbo.Teachers', 'U')          IS NOT NULL DROP TABLE dbo.Teachers;
IF OBJECT_ID('dbo.Roles', 'U')             IS NOT NULL DROP TABLE dbo.Roles;
GO

-- =============================================
-- 2. TẠO CÁC BẢNG NỀN TẢNG (CREATE TABLES)
-- =============================================


-- BẢNG 1: GIÁO VIÊN (Teachers)
CREATE TABLE dbo.Teachers (
    id          INT IDENTITY(1,1)   NOT NULL,
    name        NVARCHAR(255)       NOT NULL,
    dob         DATE                NULL,
    phone       VARCHAR(20)         NULL,
    email       VARCHAR(255)        NULL,
    address     NVARCHAR(500)       NULL,
    status      VARCHAR(20)         NOT NULL DEFAULT 'active',
    CONSTRAINT PK_Teachers PRIMARY KEY CLUSTERED (id ASC)
);
GO

-- BẢNG 2: TÀI KHOẢN NGUỜI DÙNG (Users)
CREATE TABLE dbo.Users (
    id          INT IDENTITY(1,1)   NOT NULL,
    username    VARCHAR(255)        NOT NULL,
    password    VARCHAR(255)        NOT NULL,
    role        VARCHAR(50)         NOT NULL DEFAULT 'ROLE_TEACHER',
    enabled     BIT                 NOT NULL DEFAULT 1,
    teacherId   INT                 NULL,
    avatarUrl   VARCHAR(500)        NULL,
    CONSTRAINT PK_Users PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT UQ_Users_Username UNIQUE (username),
    CONSTRAINT FK_Users_Teachers FOREIGN KEY (teacherId) REFERENCES dbo.Teachers(id) ON DELETE SET NULL
);
GO

-- BẢNG 3: TRƯỜNG MẦM NON (Schools)
CREATE TABLE dbo.Schools (
    id            INT IDENTITY(1,1) NOT NULL,
    name          NVARCHAR(255)     NOT NULL,
    address       NVARCHAR(500)     NULL,
    contactPerson NVARCHAR(255)     NULL,
    phone         VARCHAR(20)       NULL,
    CONSTRAINT PK_Schools PRIMARY KEY CLUSTERED (id ASC)
);
GO

-- BẢNG 4: LỚP HỌC AEROBIC (Classes)
CREATE TABLE dbo.Classes (
    id              INT IDENTITY(1,1) NOT NULL,
    name            NVARCHAR(255)     NOT NULL,
    schoolId        INT               NOT NULL,
    studentCount    INT               NOT NULL DEFAULT 20,
    standardPeriods INT               NOT NULL DEFAULT 2,
    CONSTRAINT PK_Classes PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_Classes_Schools FOREIGN KEY (schoolId) REFERENCES dbo.Schools(id) ON DELETE CASCADE
);
GO

-- BẢNG 5: LỊCH GIẢNG DẠY TUẦN (Schedules)
CREATE TABLE dbo.Schedules (
    id              INT IDENTITY(1,1) NOT NULL,
    dayOfWeek       INT               NOT NULL, -- 2: Thứ 2, ..., 8: Chủ nhật
    session         NVARCHAR(50)      NOT NULL, -- Ca 1, Ca 2, Ca 3, Ca 4
    teacherId       INT               NOT NULL,
    schoolId        INT               NOT NULL,
    classId         INT               NOT NULL,
    periods         INT               NOT NULL DEFAULT 2,
    startTime       TIME(0)           NOT NULL,
    endTime         TIME(0)           NOT NULL,
    CONSTRAINT PK_Schedules PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_Schedules_Teachers FOREIGN KEY (teacherId) REFERENCES dbo.Teachers(id) ON DELETE CASCADE,
    CONSTRAINT FK_Schedules_Schools  FOREIGN KEY (schoolId)  REFERENCES dbo.Schools(id)  ON DELETE NO ACTION,
    CONSTRAINT FK_Schedules_Classes  FOREIGN KEY (classId)   REFERENCES dbo.Classes(id)  ON DELETE NO ACTION
);
GO

-- BẢNG 6: NHẬT KÝ ĐIỂM DANH & CHẤM CÔNG (Attendance) - Id BIGINT để khớp với Long trong Attendance.java
CREATE TABLE dbo.Attendance (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    date        DATE                 NOT NULL,
    scheduleId  INT                  NULL,
    teacherId   INT                  NOT NULL,
    schoolId    INT                  NOT NULL,
    classId     INT                  NOT NULL,
    session     NVARCHAR(50)         NOT NULL,
    checkInTime TIME(0)             NOT NULL,
    periods     INT                  NOT NULL DEFAULT 2,
    selfieImage VARCHAR(500)         NULL,
    notes       NVARCHAR(MAX)        NULL,
    status      VARCHAR(20)          NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    CONSTRAINT PK_Attendance PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_Attendance_Schedules FOREIGN KEY (scheduleId) REFERENCES dbo.Schedules(id) ON DELETE SET NULL,
    CONSTRAINT FK_Attendance_Teachers  FOREIGN KEY (teacherId)  REFERENCES dbo.Teachers(id)  ON DELETE NO ACTION,
    CONSTRAINT FK_Attendance_Schools   FOREIGN KEY (schoolId)   REFERENCES dbo.Schools(id)   ON DELETE NO ACTION,
    CONSTRAINT FK_Attendance_Classes   FOREIGN KEY (classId)    REFERENCES dbo.Classes(id)    ON DELETE NO ACTION
);
GO

-- BẢNG 7: YÊU CẦU XIN NGHĨ & DẠY THAY (ChangeRequests)
CREATE TABLE dbo.ChangeRequests (
    id                  INT IDENTITY(1,1) NOT NULL,
    teacherId           INT               NOT NULL,
    requestType         VARCHAR(50)       NOT NULL, -- LEAVE, SUBSTITUTE, CHANGE
    date                DATE              NOT NULL,
    session             NVARCHAR(50)      NOT NULL,
    scheduleId          INT               NULL,
    reason              NVARCHAR(MAX)     NOT NULL,
    status              VARCHAR(20)       NOT NULL DEFAULT 'pending', -- pending, approved, rejected
    createdAt           DATETIME          NOT NULL DEFAULT GETDATE(),
    adminNotes          NVARCHAR(MAX)     NULL,
    documentUrl         VARCHAR(500)      NULL,
    CONSTRAINT PK_ChangeRequests PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_ChangeRequests_Teachers  FOREIGN KEY (teacherId)  REFERENCES dbo.Teachers(id)  ON DELETE NO ACTION,
    CONSTRAINT FK_ChangeRequests_Schedules FOREIGN KEY (scheduleId) REFERENCES dbo.Schedules(id) ON DELETE SET NULL
);
GO

-- BẢNG 8: KHIẾU NẠI CHẤM CÔNG (Complaints) - attendanceId BIGINT khớp với Attendance.id
CREATE TABLE dbo.Complaints (
    id              INT IDENTITY(1,1) NOT NULL,
    attendanceId    BIGINT            NOT NULL,
    content         NVARCHAR(MAX)     NOT NULL,
    expectedPeriods INT               NOT NULL DEFAULT 2,
    status          INT               NOT NULL DEFAULT 0, -- 0: Chờ xử lý, 1: Đã giải quyết, 2: Từ chối
    adminNotes      NVARCHAR(MAX)     NULL,
    resolvedAt      DATETIME          NULL,
    CONSTRAINT PK_Complaints PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_Complaints_Attendance FOREIGN KEY (attendanceId) REFERENCES dbo.Attendance(id) ON DELETE CASCADE
);
GO

-- BẢNG 9: THÔNG BÁO HỆ THỐNG (Notifications) - Id BIGINT để khớp với Long trong Notification.java
CREATE TABLE dbo.Notifications (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    message     NVARCHAR(MAX)        NOT NULL,
    link        VARCHAR(255)         NULL,
    forAdmin    BIT                  NOT NULL DEFAULT 0,
    teacherId   INT                  NULL,
    isRead      BIT                  NOT NULL DEFAULT 0,
    createdAt   DATETIME             NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_Notifications PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_Notifications_Teachers FOREIGN KEY (teacherId) REFERENCES dbo.Teachers(id) ON DELETE CASCADE
);
GO

-- BẢNG 10: NHẬT KÝ HỆ THỐNG (SystemLogs)
CREATE TABLE dbo.SystemLogs (
    id          INT IDENTITY(1,1)   NOT NULL,
    userId      INT                 NULL,
    role        VARCHAR(50)         NULL,
    action      NVARCHAR(255)       NOT NULL,
    details     NVARCHAR(MAX)       NULL,
    timestamp   DATETIME            NOT NULL DEFAULT GETDATE(),
    CONSTRAINT PK_SystemLogs PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_SystemLogs_Users FOREIGN KEY (userId) REFERENCES dbo.Users(id) ON DELETE SET NULL
);
GO

-- BẢNG 11: BIỂU MẪU & CHỨNG TỪ (DocumentTemplates) - Cập nhật đồng bộ cột với DocumentTemplate.java
CREATE TABLE dbo.DocumentTemplates (
    id            INT IDENTITY(1,1)   NOT NULL,
    name          NVARCHAR(255)       NOT NULL,
    description   NVARCHAR(MAX)       NULL,
    fileName      NVARCHAR(255)       NULL,
    fileType      VARCHAR(50)         NOT NULL DEFAULT 'DOCX',
    fileUrl       VARCHAR(500)        NOT NULL,
    downloadCount INT                 NOT NULL DEFAULT 0,
    createdAt     DATETIME            NOT NULL DEFAULT GETDATE(),
    userId        INT                 NULL,
    CONSTRAINT PK_DocumentTemplates PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT FK_DocumentTemplates_Users FOREIGN KEY (userId) REFERENCES dbo.Users(id) ON DELETE SET NULL
);
GO

-- BẢNG 12: TOKEN ĐẶT LẠI MẬT KHẨU (PasswordResetTokens)
CREATE TABLE dbo.PasswordResetTokens (
    id          INT IDENTITY(1,1)   NOT NULL,
    token       VARCHAR(255)        NOT NULL,
    userId      INT                 NOT NULL,
    expiryDate  DATETIME            NOT NULL,
    CONSTRAINT PK_PasswordResetTokens PRIMARY KEY CLUSTERED (id ASC),
    CONSTRAINT UQ_PasswordResetTokens_Token UNIQUE (token),
    CONSTRAINT FK_PasswordResetTokens_Users FOREIGN KEY (userId) REFERENCES dbo.Users(id) ON DELETE CASCADE
);
GO

-- =============================================
-- DỮ LIỆU MẪU BAN ĐẦU (FULL SEED DATA DEMO)
-- =============================================

-- Mật khẩu mặc định cho tất cả tài khoản: 123456
-- Hash BCrypt: $2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS


-- 1. Thêm Giáo viên
SET IDENTITY_INSERT dbo.Teachers ON;
INSERT INTO dbo.Teachers (id, name, dob, phone, email, address, status) VALUES
(1, N'Nguyễn Thị Mai',   '1995-03-12', '0912345678', 'mainguyen@gmail.com',  N'Quận 1, TP. HCM',      'active'),
(2, N'Trần Minh Hoàng',  '1998-07-24', '0987654321', 'hoangtran@gmail.com',  N'Quận 3, TP. HCM',      'active'),
(3, N'Phạm Thanh Thảo',  '2000-11-05', '0905111222', 'thaopham@gmail.com',   N'Bình Thạnh, TP. HCM',  'active'),
(4, N'Đặng Hồng Hạnh',   '1993-05-15', '0933444555', 'hanhdang@gmail.com',   N'Phú Nhuận, TP. HCM',   'active'),
(5, N'Vũ Hoàng Long',    '1992-09-20', '0944555666', 'longvu@gmail.com',     N'Tân Bình, TP. HCM',    'active'),
(6, N'Lê Thị Kim Anh',   '1997-12-08', '0977888999', 'kimanhle@gmail.com',   N'Gò Vấp, TP. HCM',      'active');
SET IDENTITY_INSERT dbo.Teachers OFF;
GO

-- 2. Thêm Tài khoản người dùng (Users)
SET IDENTITY_INSERT dbo.Users ON;
INSERT INTO dbo.Users (id, username, password, role, enabled, teacherId) VALUES
(1, 'teacher.mai@gmail.com',      '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_TEACHER', 1, 1),
(2, 'teacher.hoang@gmail.com',    '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_TEACHER', 1, 2),
(3, 'teacher.thao@gmail.com',     '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_TEACHER', 1, 3),
(4, 'admin.trungtam@gmail.com',   '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_ADMIN',   1, NULL),
(5, 'both.hanh@gmail.com',        '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_ADMIN',   1, 4),
(6, 'admin.long@gmail.com',       '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_ADMIN',   1, 5),
(7, 'canhthang457@gmail.com',      '$2a$10$XIML3Qn5f6CEzhrD6lgdmekK2v4dbZId3mlOPtFfPR06gSm6JnyDS', 'ROLE_TEACHER', 1, 6);
SET IDENTITY_INSERT dbo.Users OFF;
GO

-- 3. Thêm Trường Mầm Non
SET IDENTITY_INSERT dbo.Schools ON;
INSERT INTO dbo.Schools (id, name, address, contactPerson, phone) VALUES
(1, N'Trường Mầm Non Hoa Mai',         N'123 Đường Nguyễn Huệ, Quận 1, TP. HCM',        N'Cô Hoa',   '0911223344'),
(2, N'Trường Mầm Non Hướng Dương',     N'456 Đường Cách Mạng Tháng 8, Quận 3, TP. HCM', N'Thầy Hải', '0922334455'),
(3, N'Trường Mầm Non Vĩnh Lộc',        N'789 Đường Lê Trọng Tấn, Bình Tân, TP. HCM',    N'Cô Lan',   '0933445566'),
(4, N'Trường Mầm Non Tuổi Thần Tiên',  N'101 Đường Cộng Hòa, Tân Bình, TP. HCM',       N'Cô Minh',  '0944556677');
SET IDENTITY_INSERT dbo.Schools OFF;
GO

-- 4. Thêm Lớp Học Aerobic
SET IDENTITY_INSERT dbo.Classes ON;
INSERT INTO dbo.Classes (id, name, schoolId, studentCount, standardPeriods) VALUES
(1, N'Lớp Aerobic Chồi 1',    1, 20, 2),
(2, N'Lớp Aerobic Lá 2',      2, 25, 2),
(3, N'Lớp Aerobic Mầm 3',     1, 18, 2),
(4, N'Lớp Aerobic Chồi 2',    3, 22, 2),
(5, N'Lớp Aerobic Lá 1',      4, 24, 2),
(6, N'Lớp Aerobic Năng Khiếu', 2, 15, 2);
SET IDENTITY_INSERT dbo.Classes OFF;
GO

-- 5. Thêm Lịch Giảng Dạy Tuần (Đủ 4 Ca: Ca 1, Ca 2, Ca 3, Ca 4)
SET IDENTITY_INSERT dbo.Schedules ON;
INSERT INTO dbo.Schedules (id, dayOfWeek, session, teacherId, schoolId, classId, periods, startTime, endTime) VALUES
(1, 2, N'Ca 1', 1, 1, 1, 2, '08:00:00', '09:30:00'),
(2, 3, N'Ca 2', 2, 2, 2, 2, '09:45:00', '11:15:00'),
(3, 4, N'Ca 3', 3, 1, 3, 2, '14:00:00', '15:30:00'),
(4, 5, N'Ca 4', 1, 2, 6, 2, '15:45:00', '17:15:00'),
(5, 6, N'Ca 1', 5, 3, 4, 2, '08:00:00', '09:30:00'),
(6, 7, N'Ca 2', 6, 4, 5, 2, '09:45:00', '11:15:00'),
(7, 2, N'Ca 3', 2, 3, 4, 2, '14:00:00', '15:30:00'),
(8, 4, N'Ca 4', 3, 4, 5, 2, '15:45:00', '17:15:00');
SET IDENTITY_INSERT dbo.Schedules OFF;
GO

-- 6. Thêm Nhật Ký Điểm Danh & Chấm Công
SET IDENTITY_INSERT dbo.Attendance ON;
INSERT INTO dbo.Attendance (id, date, scheduleId, teacherId, schoolId, classId, session, checkInTime, periods, selfieImage, notes, status) VALUES
(1, '2026-07-06', 1, 1, 1, 1, N'Ca 1', '07:55:00', 2, 'https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=400&q=80', N'Hoàn thành tốt ca dạy', 'APPROVED'),
(2, '2026-07-07', 2, 2, 2, 2, N'Ca 2', '09:40:00', 2, 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=400&q=80', N'Lớp ngoan, học tốt', 'PENDING'),
(3, '2026-07-08', 3, 3, 1, 3, N'Ca 3', '13:55:00', 2, 'https://images.unsplash.com/photo-1580894732413-b73a7ec9cf35?auto=format&fit=crop&w=400&q=80', N'Dạy ca chiều hoàn thành đúng giờ', 'APPROVED'),
(4, '2026-07-09', 4, 1, 2, 6, N'Ca 4', '15:40:00', 2, 'https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=400&q=80', N'Dạy bổ trợ Ca 4 hoàn thành tốt', 'APPROVED'),
(5, '2026-07-10', 5, 5, 3, 4, N'Ca 1', '07:58:00', 2, 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=400&q=80', N'Các bé tập hăng hái', 'APPROVED'),
(6, '2026-07-11', 6, 6, 4, 5, N'Ca 2', '09:42:00', 2, 'https://images.unsplash.com/photo-1580894732413-b73a7ec9cf35?auto=format&fit=crop&w=400&q=80', N'Dạy bù tiết cho tuần trước', 'PENDING');
SET IDENTITY_INSERT dbo.Attendance OFF;
GO

-- 7. Thêm Yêu Cầu Xin Nghỉ & Dạy Thay (ChangeRequests)
SET IDENTITY_INSERT dbo.ChangeRequests ON;
INSERT INTO dbo.ChangeRequests (id, teacherId, requestType, date, session, scheduleId, reason, status, createdAt, adminNotes, documentUrl) VALUES
(1, 1, 'LEAVE',      '2026-07-13', N'Ca 1', 1, N'Xin nghỉ phép giải quyết việc gia đình', 'pending', GETDATE(), NULL, NULL),
(2, 2, 'SUBSTITUTE', '2026-07-14', N'Ca 2', 2, N'Nghỉ ốm đau chân, nhờ cô Mai dạy thay', 'approved', GETDATE(), N'Đã duyệt đồng ý phân công dạy thay', NULL),
(3, 3, 'CHANGE',     '2026-07-15', N'Ca 3', 3, N'Xin đổi từ Ca 3 sang Ca 4 do bận lịch họp', 'pending', GETDATE(), NULL, NULL),
(4, 5, 'LEAVE',      '2026-07-17', N'Ca 1', 5, N'Xin nghỉ đột xuất', 'rejected', GETDATE(), N'Không có giáo viên thay thế trong khung giờ này', NULL);
SET IDENTITY_INSERT dbo.ChangeRequests OFF;
GO

-- 8. Thêm Khiếu Nại Chấm Công (Complaints)
SET IDENTITY_INSERT dbo.Complaints ON;
INSERT INTO dbo.Complaints (id, attendanceId, content, expectedPeriods, status, adminNotes, resolvedAt) VALUES
(1, 2, N'Hệ thống chưa hiển thị đúng số tiết của buổi dạy lớp Aerobic Lá 2', 2, 0, NULL, NULL),
(2, 1, N'Khiếu nại kiểm tra nhầm ca dạy', 2, 1, N'Đã đối soát danh sách và điều chỉnh thông tin', GETDATE()),
(3, 6, N'Số tiết ghi nhận 1 tiết thay vì 2 tiết chuẩn', 2, 0, NULL, NULL);
SET IDENTITY_INSERT dbo.Complaints OFF;
GO

-- 9. Thêm Thông Báo (Notifications)
SET IDENTITY_INSERT dbo.Notifications ON;
INSERT INTO dbo.Notifications (id, message, link, forAdmin, teacherId, isRead, createdAt) VALUES
(1, N'Có khiếu nại chấm công mới từ thầy Hoàng (lớp Aerobic Lá 2)', '/admin/complaints', 1, NULL, 0, GETDATE()),
(2, N'Có đơn xin dạy thay mới cần phê duyệt từ thầy Hoàng', '/admin/change-requests', 1, NULL, 0, GETDATE()),
(3, N'Có khiếu nại số tiết dạy mới từ cô Kim Anh (lớp Aerobic Lá 1)', '/admin/complaints', 1, NULL, 0, GETDATE()),
(4, N'Đơn xin nghỉ phép ngày 2026-07-13 của bạn đang chờ duyệt', '/teacher/change-requests', 0, 1, 0, GETDATE()),
(5, N'Đơn xin dạy thay ngày 2026-07-14 của bạn đã được duyệt', '/teacher/change-requests', 0, 2, 1, GETDATE());
SET IDENTITY_INSERT dbo.Notifications OFF;
GO

-- 10. Thêm Nhật Ký Hệ Thống (SystemLogs)
SET IDENTITY_INSERT dbo.SystemLogs ON;
INSERT INTO dbo.SystemLogs (id, userId, role, action, details, timestamp) VALUES
(1, 1, 'TEACHER', N'Chấm công',        N'Báo cáo chấm công lớp Chồi 1 thành công', GETDATE()),
(2, 4, 'ADMIN',   N'Duyệt đơn từ',     N'Đã duyệt đơn dạy thay của thầy Hoàng', GETDATE()),
(3, 6, 'ADMIN',   N'Tạo lịch dạy',     N'Tạo mới lịch giảng dạy cho cô Kim Anh', GETDATE()),
(4, 5, 'ADMIN',   N'Xử lý khiếu nại',  N'Đã xử lý khiếu nại chấm công mã #2', GETDATE());
SET IDENTITY_INSERT dbo.SystemLogs OFF;
GO

-- 11. Thêm Biểu Mẫu Chứng Từ (DocumentTemplates)
SET IDENTITY_INSERT dbo.DocumentTemplates ON;
INSERT INTO dbo.DocumentTemplates (id, name, description, fileName, fileType, fileUrl, downloadCount, createdAt, userId) VALUES
(1, N'Mẫu đơn xin nghỉ phép / Dạy thay', N'Biểu mẫu chuẩn dùng cho Giáo viên xin nghỉ hoặc đăng ký dạy thay', N'don_xin_nghi_phep.docx', 'DOCX', '/uploads/templates/don_xin_nghi_phep.docx', 12, GETDATE(), 4),
(2, N'Báo cáo tổng hợp số tiết dạy tháng', N'Mẫu file Excel đối soát số tiết giảng dạy hàng tháng cho Giáo viên', N'bao_cao_tiet_day.xlsx', 'XLSX', '/uploads/templates/bao_cao_tiet_day.xlsx', 28, GETDATE(), 4),
(3, N'Mẫu đơn khiếu nại chấm công', N'Mẫu chứng từ gửi Ban quản lý xử lý sai lệch giờ/tiết chấm công', N'don_khieu_nai_cham_cong.docx', 'DOCX', '/uploads/templates/don_khieu_nai_cham_cong.docx', 8, GETDATE(), 4);
SET IDENTITY_INSERT dbo.DocumentTemplates OFF;
GO
